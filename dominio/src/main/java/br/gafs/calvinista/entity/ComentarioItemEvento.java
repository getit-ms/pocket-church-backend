package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusComentarioItemEvento;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_comentario_item_evento")
@EqualsAndHashCode(of = {"id", "igreja"})
@NamedQueries({
        @NamedQuery(name = "ComentarioItemEvento.findDenunciados", query = "select c, count(d.id) from DenunciaComentarioItemEvento  d inner join d.comentario c where d.igreja.chave = :igreja and d.status = :statusDenuncia and c.status = :statusComentario group by c order by count(d.id) desc, min(d.dataHoraDenuncia)")
})
public class ComentarioItemEvento implements IEntity {
    @Id
    @Column(name = "id_comentario_item_evento")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_comentario_item_evento")
    @SequenceGenerator(sequenceName = "seq_comentario_item_evento", name = "seq_comentario_item_evento")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Column(name = "data_hora")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = new Date();

    @NotEmpty
    @Column(name = "comentario")
    @View.MergeViews(View.Cadastro.class)
    private String comentario;

    @Setter
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Membro membro;

    @JsonIgnore
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusComentarioItemEvento status = StatusComentarioItemEvento.PUBLICADO;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    private Igreja igreja;

    @Setter
    @ManyToOne
    @JsonView(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "id_item_evento", referencedColumnName = "id_item_evento"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "tipo_item_evento", referencedColumnName = "tipo"),
    })
    private ItemEvento itemEvento;

    @Setter
    @Transient
    private Integer quantidadeDenuncias;

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;
        if (igreja != null) {
            this.chaveIgreja = igreja.getChave();
        } else {
            this.chaveIgreja = null;
        }
    }

    public void censurado() {
        status = StatusComentarioItemEvento.CENSURADO;
    }
}
