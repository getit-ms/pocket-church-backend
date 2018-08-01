/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusDocumento;
import br.gafs.pocket.corporate.entity.domain.TipoDocumento;
import br.gafs.pocket.corporate.view.View;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.*;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_documento")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@NamedQueries({
    @NamedQuery(name = "Documento.findEmpresaNaoDivultadosByDataPublicacao", query = "select i from Documento e inner join e.empresa i where i.status = :statusEmpresa and e.divulgado = false and e.dataPublicacao <= :data group by i"),
    @NamedQuery(name = "Documento.updateNaoDivulgadosByEmpresa", query = "update Documento e set e.divulgado = true where e.empresa.chave = :empresa and e.dataPublicacao <= :data "),
    @NamedQuery(name = "Documento.findPDFByStatus", query = "select e from Documento e where e.pdf is not null and e.status = :status order by e.dataPublicacao"),
    @NamedQuery(name = "Documento.updateStatus", query = "update Documento e set e.status = :status where e.id = :documento and e.empresa.chave = :empresa")
})
public class Documento implements IEntity, ArquivoPDF {

    private static final Map<RegistroEmpresaId, Integer> locks = new HashMap<RegistroEmpresaId, Integer>();

    public synchronized static boolean locked(RegistroEmpresaId id){
        return locks.containsKey(id);
    }

    public synchronized static int lock(RegistroEmpresaId id){
        return locks.get(id);
    }

    public synchronized static void lock(RegistroEmpresaId id, int percent){
        locks.put(id, percent);
    }

    public synchronized static void unlock(RegistroEmpresaId id){
        locks.remove(id);
    }

    public synchronized static int locked(){
        return locks.size();
    }

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_documento")
    @SequenceGenerator(sequenceName = "seq_documento", name = "seq_documento")
    @GeneratedValue(generator = "seq_documento", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Setter
    @NotEmpty
    @Length(max = 250)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "titulo", length = 250, nullable = false)
    private String titulo;
    
    @Setter
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;
    
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.DATE)
    @Column(name = "data", nullable = false)
    private Date data = new Date();
    
    @Setter
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    private Date dataPublicacao;

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_pdf", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo pdf;

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @Setter
    @ManyToOne
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria_documento"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "CHAVE_EMPRESA", insertable = false, updatable = false)
    })
    private CategoriaDocumento categoria;

    @Setter
    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "rl_documento_paginas", joinColumns = {
            @JoinColumn(name = "id_documento", referencedColumnName = "id_documento", nullable = false),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
    }, inverseJoinColumns = {
            @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private List<Arquivo> paginas = new ArrayList<Arquivo>();

    @JsonView(View.Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusDocumento status = StatusDocumento.PROCESSANDO;
    
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "autor", nullable = false)
    private String autor;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_alteracao")
    private Date ultimaAlteracao = new Date();

    @JsonView(View.Detalhado.class)
    @Column(name = "divulgado", nullable = false)
    private boolean divulgado;
    
    @ManyToOne
    @Setter(onMethod = @_(@JsonIgnore))
    @Getter(onMethod = @_(@JsonProperty))
    @JoinColumns({
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false, insertable = false, updatable = false)
    })
    private Colaborador colaborador;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;

    public Documento(Colaborador colaborador) {
        this.colaborador = colaborador;
        this.empresa = colaborador.getEmpresa();
    }
    
    public boolean isEmEdicao(){
        return dataPublicacao == null;
    }
    
    public String getFilename(){
        return StringUtil.formataValor(titulo, true, false).replace(" ", "_");
    }

    public void notificado(){
        divulgado = true;
    }

    public void alterado(){
        ultimaAlteracao = new Date();
    }

    public void processando() {
        status = StatusDocumento.PROCESSANDO;
    }

    public void publicado() {
        status = StatusDocumento.PUBLICADO;
    }

    public boolean isAgendado() {
        return !isEmEdicao() && StatusDocumento.PUBLICADO == status
                && DateUtil.getDataAtual().before(dataPublicacao);
    }

    public boolean isPublicado() {
        return !isEmEdicao() && StatusDocumento.PUBLICADO == status
                && DateUtil.getDataAtual().after(dataPublicacao);
    }

    public boolean isProcessando() {
        return !isEmEdicao() && StatusDocumento.PROCESSANDO == status;
    }

    public boolean isRejeitado() {
        return !isEmEdicao() && StatusDocumento.REJEITADO == status;
    }

    public double getPorcentagemProcessamento(){
        RegistroEmpresaId riid = new RegistroEmpresaId(chaveEmpresa, id);
        if (isProcessando() && locked(riid)){
            return lock(riid);
        }

        if (isPublicado() || isAgendado()){
            return 1d;
        }

        return 0d;
    }

    @JsonProperty
    @JsonView(View.Resumido.class)
    public List<Arquivo> getPaginas() {
        Collections.sort(paginas);
        return paginas;
    }

    @Override
    public Arquivo getPDF() {
        return pdf;
    }

    public TipoDocumento getTipo() {
        if (pdf == null) {
            return TipoDocumento.TEXTO;
        } else {
            return TipoDocumento.PDF;
        }
    }
}
