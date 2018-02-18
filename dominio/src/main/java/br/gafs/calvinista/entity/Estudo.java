/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusEstudo;
import br.gafs.calvinista.entity.dominio.TipoEstudo;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    @NamedQuery(name = "Estudo.findIgrejaNaoDivultadosByDataPublicacao", query = "select i from Estudo e inner join e.igreja i where e.igreja.status = :statusIgreja and e.divulgado = false and e.dataPublicacao <= :data group by i"),
    @NamedQuery(name = "Estudo.updateNaoDivulgadosByIgreja", query = "update Estudo e set e.divulgado = true where e.igreja.chave = :igreja"),
    @NamedQuery(name = "Estudo.findPDFByStatus", query = "select e from Estudo e where e.pdf is not null and e.status = :status order by e.dataPublicacao"),
    @NamedQuery(name = "Estudo.updateStatus", query = "update Estudo e set e.status = :status where e.id = :estudo and e.igreja.chave = :igreja")
})
public class Estudo implements IEntity, ArquivoPDF {

    private static final Map<RegistroIgrejaId, Integer> locks = new HashMap<RegistroIgrejaId, Integer>();

    public synchronized static boolean locked(RegistroIgrejaId id){
        return locks.containsKey(id);
    }

    public synchronized static int lock(RegistroIgrejaId id){
        return locks.get(id);
    }

    public synchronized static void lock(RegistroIgrejaId id, int percent){
        locks.put(id, percent);
    }

    public synchronized static void unlock(RegistroIgrejaId id){
        locks.remove(id);
    }

    public synchronized static int locked(){
        return locks.size();
    }

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
    
    @JsonView(Detalhado.class)
    public boolean isEmEdicao(){
        return dataPublicacao == null || DateUtil.getDataAtual().before(dataPublicacao);
    }
    
    @JsonView(Detalhado.class)
    public boolean isPublicado(){
        return !isEmEdicao();
    }
    
    public String getFilename(){
        return titulo.replaceAll("[^a-zA-Z0-9]", "_");
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
