/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusEstudo;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

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
public class Estudo implements IEntity {
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
    @NotEmpty
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;
    
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data", nullable = false)
    private Date data = new Date();
    
    @Setter
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    private Date dataPublicacao;
    
    @JsonView(Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusEstudo status = StatusEstudo.EM_EDICAO;
    
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "autor", nullable = false)
    private String autor;
    
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
        return StatusEstudo.EM_EDICAO.equals(status);
    }
    
    @JsonView(Detalhado.class)
    public boolean isPublicado(){
        return StatusEstudo.PUBLICADO.equals(status);
    }
    
    public void publica(){
        if (dataPublicacao == null){
            dataPublicacao = DateUtil.getDataAtual();
        }
        status = StatusEstudo.PUBLICADO;
    }
}
