package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@IdClass(EstatisticaDispositivoId.class)
@Table(name = "tb_estatistica_dispositivo")
@EqualsAndHashCode(of = {"igreja", "data", "tipoDispositivo"})
@NamedQueries({
        @NamedQuery(name = "EstatisticaDispositivo.quantidadeDispositivosIgreja", query = "select new br.gafs.calvinista.dto.QuantidadeDispositivoDTO(d.tipo, count(d.uuid), count(distinct m.id)) from Dispositivo d inner join d.igreja i left join d.membro m where d.tipo in :tipos and i.chave = :igreja and d.ultimoAcesso > :limite group by i, d.tipo "),
        @NamedQuery(name = "EstatisticaDispositivo.findByIgreja", query = "select ed from EstatisticaDispositivo ed where ed.igreja.chave = :igreja order by ed.data"),
        @NamedQuery(name = "EstatisticaDispositivo.findOnLine", query = "select new br.gafs.calvinista.entity.EstatisticaDispositivo(i, CURRENT_DATE, d.tipo, count(d.uuid), count(distinct m.id)) from Dispositivo d inner join d.igreja i left join d.membro m where d.tipo in :tipos and i.status = :statusIgreja and d.ultimoAcesso > :limite group by i, d.tipo "),
        @NamedQuery(name = "EstatisticaDispositivo.removeAntigas", query = "delete from EstatisticaDispositivo e where e.data < :limite")
})
public class EstatisticaDispositivo implements IEntity {
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Id
    @Column(name = "data")
    @Temporal(TemporalType.DATE)
    @View.JsonTemporal(View.JsonTemporalType.DATE)
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
    public EstatisticaDispositivoId getId() {
        return new EstatisticaDispositivoId(igreja.getChave(), data, tipoDispositivo);
    }
}
