package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusDenunciaComentarioItemEvento;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@EqualsAndHashCode(of = {"id", "igreja"})
@Table(name = "tb_denuncia_comentario_item_evento")
@NamedQueries({
        @NamedQuery(name = "DenunciaComentarioItemEvento.findByComentario", query = "select d from DenunciaComentarioItemEvento  d where d.igreja.chave = :igreja and d.status = :statusDenuncia and d.comentario.id = :comentario order by d.dataHoraDenuncia")
})
public class DenunciaComentarioItemEvento implements IEntity {
    @Id
    @Column(name = "id_denuncia_comentario_item_evento")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_denuncia_comentario_item_evento")
    @SequenceGenerator(name = "seq_denuncia_comentario_item_evento", sequenceName = "seq_denuncia_comentario_item_evento")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Column(name = "data_hora_denuncia")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraDenuncia = new Date();

    @NotEmpty
    @Column(name = "justificativa")
    @View.MergeViews(View.Cadastro.class)
    private String justificativa;

    @Column(name = "data_hora_analise")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraAnalise;

    @Setter
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_denunciante", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Membro denunciante;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_analista", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Membro analista;

    @JsonIgnore
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusDenunciaComentarioItemEvento status = StatusDenunciaComentarioItemEvento.PENDENTE;

    @Setter
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "id_comentario_item_evento", referencedColumnName = "id_comentario_item_evento"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private ComentarioItemEvento comentario;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    private Igreja igreja;

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;
        if (igreja != null) {
            this.chaveIgreja = igreja.getChave();
        } else {
            this.chaveIgreja = null;
        }
    }

    public void atende(Membro analista) {
        this.status = StatusDenunciaComentarioItemEvento.ATENDIDO;
        this.analista = analista;
        this.dataHoraAnalise = new Date();
        this.comentario.censurado();
    }

    public void rejeita(Membro analista) {
        this.status = StatusDenunciaComentarioItemEvento.REJEITADO;
        this.analista = analista;
        this.dataHoraAnalise = new Date();
    }
}
