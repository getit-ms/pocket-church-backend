package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusItemTimeline;
import br.gafs.calvinista.entity.domain.TipoItemTimeline;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ItemTimelineId.class)
@ToString(of = {"id", "chaveIgreja", "tipo"})
@Table(name = "tb_item_timeline")
@EqualsAndHashCode(of = {"id", "chaveIgreja", "tipo"})
public class ItemTimeline implements IEntity {
    @Id
    @Column(name = "id_item_timeline")
    @SequenceGenerator(name = "seq_item_timeline", sequenceName = "seq_item_timeline")
    @GeneratedValue(generator = "seq_item_timeline", strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty
    @Length(max = 250)
    @Column(name = "titulo", length = 250, nullable = false)
    private String titulo;

    @NotEmpty
    @Column(name = "descricao")
    private String titulo;


    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", insertable = false, updatable = false)
    private TipoItemTimeline tipo;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data", nullable = false)
    private Date data;

    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusItemTimeline status = StatusItemTimeline.ATIVO;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @OneToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;
}
