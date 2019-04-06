package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EstatisticaId.class)
@Table(name = "tb_estatistica")
@EqualsAndHashCode(of = {"igreja", "data", "tipoDispositivo"})
@NamedQueries({
        @NamedQuery(name = "Estatistica.findByIgreja", query = "select new br.gafs.calvinista.entity.Estatistica(i, CURRENT_DATE, d.tipo, count(d.uuid), count(distinct m.id)) from Dispositivo d inner join d.igreja i left join d.membro m where d.tipo in :tipos and i.status = :statusIgreja group by i, d.tipo "),
        @NamedQuery(name = "Estatistica.removeAntigas", query = "delete from Estatistica e where e.data < :limite")
})
public class Estatistica implements IEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Id
    @Column(name = "data")
    @Temporal(TemporalType.DATE)
    private Date data;

    @Id
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "tipo_dispositivo")
    private TipoDispositivo tipoDispositivo;

    @Column(name = "quantidade_dispositivos")
    private Long quantidadeDispositivos;

    @Column(name = "quantidade_membros_logados")
    private Long quantidadeMembrosLogados;

    @Override
    public EstatisticaId getId() {
        return new EstatisticaId(igreja.getChave(), data, tipoDispositivo);
    }
}
