package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.pocket.corporate.view.View;
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
@EqualsAndHashCode(of = {"empresa", "data", "tipoDispositivo"})
@NamedQueries({
        @NamedQuery(name = "EstatisticaDispositivo.quantidadeDispositivosEmpresa", query = "select new br.gafs.pocket.corporate.dto.QuantidadeDispositivoDTO(d.tipo, count(d.uuid), count(distinct c.id)) from Dispositivo d inner join d.empresa i left join d.colaborador c where d.tipo in :tipos and i.chave = :empresa and d.ultimoAcesso > :limite group by i, d.tipo "),
        @NamedQuery(name = "EstatisticaDispositivo.findByEmpresa", query = "select ed from EstatisticaDispositivo ed where ed.empresa.chave = :empresa order by ed.data"),
        @NamedQuery(name = "EstatisticaDispositivo.findOnLine", query = "select new br.gafs.pocket.corporate.entity.EstatisticaDispositivo(i, CURRENT_DATE, d.tipo, count(d.uuid), count(distinct c.id)) from Dispositivo d inner join d.empresa i left join d.colaborador c where d.tipo in :tipos and i.status = :statusEmpresa and d.ultimoAcesso > :limite group by i, d.tipo "),
        @NamedQuery(name = "EstatisticaDispositivo.removeAntigas", query = "delete from EstatisticaDispositivo e where e.data < :limite")
})
public class EstatisticaDispositivo implements IEntity {
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

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

    @Column(name = "quantidade_colaboradores_logados")
    private Long quantidadeColaboradoresLogados;

    @Override
    public EstatisticaDispositivoId getId() {
        return new EstatisticaDispositivoId(empresa.getChave(), data, tipoDispositivo);
    }
}
