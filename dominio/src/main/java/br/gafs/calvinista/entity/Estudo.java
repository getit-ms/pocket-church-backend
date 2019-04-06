/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusEstudo;
import br.gafs.calvinista.entity.domain.TipoEstudo;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
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
@Table(name = "tb_estudo")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
@NamedQueries({
    @NamedQuery(name = "Estudo.findIgrejaNaoDivultadosByDataPublicacao", query = "select i from Estudo e inner join e.igreja i where i.status = :statusIgreja and e.divulgado = false and e.dataPublicacao <= :data group by i"),
    @NamedQuery(name = "Estudo.findUltimoADivulgar", query = "select e from Estudo e inner join e.igreja i where i.chave = :igreja and e.divulgado = false and e.dataPublicacao <= :data order by e.dataPublicacao desc"),
    @NamedQuery(name = "Estudo.updateNaoDivulgadosByIgreja", query = "update Estudo e set e.divulgado = true where e.igreja.chave = :igreja and e.dataPublicacao <= :data "),
    @NamedQuery(name = "Estudo.findPDFByStatus", query = "select e from Estudo e where e.pdf is not null and e.status = :status order by e.dataPublicacao"),
    @NamedQuery(name = "Estudo.updateStatus", query = "update Estudo e set e.status = :status where e.id = :estudo and e.igreja.chave = :igreja")
})
public class Estudo implements IEntity, ArquivoPDF {

    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_estudo")
    @SequenceGenerator(sequenceName = "seq_estudo", name = "seq_estudo")
    @GeneratedValue(generator = "seq_estudo", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Setter
    @NotEmpty
    @Length(max = 250)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "titulo", length = 250, nullable = false)
    private String titulo;
    
    @Setter
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;
    
    @JsonView(Resumido.class)
    @Temporal(TemporalType.DATE)
    @Column(name = "data", nullable = false)
    private Date data = new Date();
    
    @Setter
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    private Date dataPublicacao;

    @Setter
    @OneToOne
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_pdf", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo pdf;

    @Setter
    @OneToOne
    @JsonView(Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @Setter
    @ManyToOne
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria_estudo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "CHAVE_IGREJA", insertable = false, updatable = false)
    })
    private CategoriaEstudo categoria;

    @Setter
    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "rl_estudo_paginas", joinColumns = {
            @JoinColumn(name = "id_estudo", referencedColumnName = "id_estudo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    }, inverseJoinColumns = {
            @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private List<Arquivo> paginas = new ArrayList<Arquivo>();

    @JsonView(Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusEstudo status = StatusEstudo.PROCESSANDO;
    
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "autor", nullable = false)
    private String autor;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_alteracao")
    private Date ultimaAlteracao = new Date();

    @JsonView(Detalhado.class)
    @Column(name = "divulgado", nullable = false)
    private boolean divulgado;
    
    @ManyToOne
    @Setter(onMethod = @_(@JsonIgnore))
    @Getter(onMethod = @_(@JsonProperty))
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false, insertable = false, updatable = false)
    })
    private Membro membro;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    public Estudo(Membro membro) {
        this.membro = membro;
        this.igreja = membro.getIgreja();
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
        status = StatusEstudo.PROCESSANDO;
    }

    public void publicado() {
        status = StatusEstudo.PUBLICADO;
    }

    public boolean isAgendado() {
        return !isEmEdicao() && StatusEstudo.PUBLICADO == status
                && DateUtil.getDataAtual().before(dataPublicacao);
    }

    public boolean isPublicado() {
        return !isEmEdicao() && StatusEstudo.PUBLICADO == status
                && DateUtil.getDataAtual().after(dataPublicacao);
    }

    public boolean isProcessando() {
        return !isEmEdicao() && StatusEstudo.PROCESSANDO == status;
    }

    public boolean isRejeitado() {
        return !isEmEdicao() && StatusEstudo.REJEITADO == status;
    }

    public double getPorcentagemProcessamento(){
        if (isProcessando()) {
            return getPaginas().size();
        }

        if (isPublicado() || isAgendado()){
            return 1d;
        }

        return 0d;
    }

    @JsonProperty
    @JsonView(Resumido.class)
    public List<Arquivo> getPaginas() {
        Collections.sort(paginas);
        return paginas;
    }

    @Override
    public Arquivo getPDF() {
        return pdf;
    }

    public TipoEstudo getTipo() {
        if (pdf == null) {
            return TipoEstudo.TEXTO;
        } else {
            return TipoEstudo.PDF;
        }
    }
}
