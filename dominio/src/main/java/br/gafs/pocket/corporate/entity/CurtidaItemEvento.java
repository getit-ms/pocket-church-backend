package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
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
@EqualsAndHashCode(of = {"idColaborador", "idItemEvento", "chaveEmpresa", "tipoItemEvento"})
public class CurtidaItemEvento implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_colaborador", insertable = false, updatable = false)
    private Long idColaborador;

    @Id
    @JsonIgnore
    @Column(name = "id_item_evento", insertable = false, updatable = false)
    private String idItemEvento;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @Id
    @JsonIgnore
    @Column(name = "tipo_item_evento", insertable = false, updatable = false)
    private TipoItemEvento tipoItemEvento;

    @Column(name = "data_hora")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = new Date();

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa"),
    })
    private Colaborador colaborador;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    private Empresa empresa;

    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_item_evento", referencedColumnName = "id_item_evento"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "tipo_item_evento", referencedColumnName = "tipo"),
    })
    private ItemEvento itemEvento;

    @Override
    public IteracaoItemEventoId getId() {
        return new IteracaoItemEventoId(idColaborador, idItemEvento, chaveEmpresa, tipoItemEvento);
    }

    public CurtidaItemEvento(Colaborador colaborador, ItemEvento itemEvento) {
        this.colaborador =colaborador;
        this.itemEvento = itemEvento;
        this.empresa = colaborador.getEmpresa();
        this.idColaborador = colaborador.getId();
        this.chaveEmpresa = colaborador.getEmpresa().getChave();
        this.idItemEvento = itemEvento.getId();
        this.tipoItemEvento = itemEvento.getTipo();
    }
}
