package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusItemEvento;
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
@IdClass(EventoCalendarioId.class)
@Table(name = "tb_evento_calendario")
@EqualsAndHashCode(of = {"id", "inicio", "chaveIgreja"})
@NamedQueries({
        @NamedQuery(name = "EventoCalendario.removeDesatualizadosPorIgreja", query = "delete from EventoCalendario ec where ec.igreja.chave = :igreja and ec.ultimaAtualizacao < :limite"),
        @NamedQuery(name = "EventoCalendario.countByIgreja", query = "select count(ec) from EventoCalendario ec where ec.igreja.chave = :igreja"),
        @NamedQuery(name = "EventoCalendario.findByIgreja", query = "select ec from EventoCalendario ec where ec.igreja.chave = :igreja order by ec.inicio, ec.id")
})
public class EventoCalendario implements IEntity, IItemEvento {
    @Id
    @Column(name = "id")
    private String id;

    @Id
    @Column(name = "data_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date inicio;

    @Column(name = "data_termino")
    @Temporal(TemporalType.TIMESTAMP)
    private Date termino;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "local")
    private String local;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_alteracao")
    private Date ultimaAtualizacao = new Date();

    @Id
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public EventoCalendario(Igreja igreja, String id,
                            Date inicio, Date termino,
                            String descricao, String local) {
        this.chaveIgreja = igreja.getChave();
        this.igreja = igreja;
        this.inicio = inicio;
        this.termino = termino;
        this.descricao = descricao;
        this.local = local;
        this.ultimaAtualizacao = new Date();
        this.id = id;
    }

    @Override
    @JsonIgnore
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId())
                .igreja(getIgreja())
                .tipo(TipoItemEvento.EVENTO_CALENDARIO)
                .titulo(getDescricao().length() > 150 ? getDescricao().substring(150) : getDescricao())
                .apresentacao(getDescricao())
                .dataHoraPublicacao(getInicio())
                .dataHoraReferencia(getInicio())
                .status(StatusItemEvento.PUBLICADO)
                .build();
    }
}
