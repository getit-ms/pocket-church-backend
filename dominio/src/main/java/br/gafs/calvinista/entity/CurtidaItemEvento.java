package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoItemEvento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@IdClass(IteracaoItemEventoId.class)
@Table(name = "tb_curtida_item_evento")
@EqualsAndHashCode(of = {"idMembro", "idItemEvento", "chaveIgreja", "tipoItemEvento"})
public class CurtidaItemEvento implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_membro", insertable = false, updatable = false)
    private Long idMembro;

    @Id
    @JsonIgnore
    @Column(name = "id_item_evento", insertable = false, updatable = false)
    private String idItemEvento;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Id
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item_evento", insertable = false, updatable = false)
    private TipoItemEvento tipoItemEvento;

    @Column(name = "data_hora")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = new Date();

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja"),
    })
    private Membro membro;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    private Igreja igreja;

    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_item_evento", referencedColumnName = "id_item_evento"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "tipo_item_evento", referencedColumnName = "tipo"),
    })
    private ItemEvento itemEvento;

    @Override
    public IteracaoItemEventoId getId() {
        return new IteracaoItemEventoId(idMembro, idItemEvento, chaveIgreja, tipoItemEvento);
    }

    public CurtidaItemEvento(Membro membro, ItemEvento itemEvento) {
        this.membro =membro;
        this.itemEvento = itemEvento;
        this.igreja = membro.getIgreja();
        this.idMembro = membro.getId();
        this.chaveIgreja = membro.getIgreja().getChave();
        this.idItemEvento = itemEvento.getId();
        this.tipoItemEvento = itemEvento.getTipo();
    }
}
