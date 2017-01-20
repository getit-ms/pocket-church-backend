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
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

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
    @NamedQuery(name = "Estudo.findIgrejaByStatusAndDataPublicacao", query = "select i from Estudo e inner join e.igreja i where e.igreja.status = :statusIgreja and e.status = :statusEstudo and e.dataPublicacao <= :data group by i"),
    @NamedQuery(name = "Estudo.updateNaoDivulgadosByIgreja", query = "update Estudo e set e.status = :status where e.igreja.chave = :igreja")
})
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
    private StatusEstudo status = StatusEstudo.NAO_NOTIFICADO;
    
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
        return dataPublicacao == null || DateUtil.getDataAtual().before(dataPublicacao);
    }
    
    @JsonView(Detalhado.class)
    public boolean isPublicado(){
        return !isEmEdicao();
    }

    public void notificado(){
        status = StatusEstudo.NOTIFICADO;
    }
}
