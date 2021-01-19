package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusComentarioItemEvento;
import br.gafs.pocket.corporate.view.View;
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
@IdClass(RegistroEmpresaId.class)
@Table(name = "tb_comentario_item_evento")
@EqualsAndHashCode(of = {"id", "empresa"})
@NamedQueries({
        @NamedQuery(name = "ComentarioItemEvento.findDenunciados", query = "select c, count(d.id) from DenunciaComentarioItemEvento  d inner join d.comentario c where d.empresa.chave = :empresa and d.status = :statusDenuncia and c.status = :statusComentario group by c order by count(d.id) desc, min(d.dataHoraDenuncia)")
})
public class ComentarioItemEvento implements IEntity {
    @Id
    @Column(name = "id_comentario_item_evento")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_comentario_item_evento")
    @SequenceGenerator(sequenceName = "seq_comentario_item_evento", name = "seq_comentario_item_evento")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

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
            @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Colaborador colaborador;

    @JsonIgnore
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusComentarioItemEvento status = StatusComentarioItemEvento.PUBLICADO;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
    private Empresa empresa;

    @Setter
    @ManyToOne
    @JsonView(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "id_item_evento", referencedColumnName = "id_item_evento"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "tipo_item_evento", referencedColumnName = "tipo"),
    })
    private ItemEvento itemEvento;

    @Setter
    @Transient
    private Integer quantidadeDenuncias;

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
        if (empresa != null) {
            this.chaveEmpresa = empresa.getChave();
        } else {
            this.chaveEmpresa = null;
        }
    }

    public void censurado() {
        status = StatusComentarioItemEvento.CENSURADO;
    }
}
