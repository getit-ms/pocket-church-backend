package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
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
@EqualsAndHashCode(of = {"id", "inicio", "chaveEmpresa"})
@NamedQueries({
        @NamedQuery(name = "EventoCalendario.removeDesatualizadosPorEmpresa", query = "delete from EventoCalendario ec where ec.empresa.chave = :empresa and ec.ultimaAtualizacao < :limite"),
        @NamedQuery(name = "EventoCalendario.countByEmpresa", query = "select count(ec) from EventoCalendario ec where ec.empresa.chave = :empresa"),
        @NamedQuery(name = "EventoCalendario.findByEmpresa", query = "select ec from EventoCalendario ec where ec.empresa.chave = :empresa order by ec.inicio, ec.id")
})
public class EventoCalendario implements IEntity {
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
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @ManyToOne
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    public EventoCalendario(Empresa empresa, String id,
                            Date inicio, Date termino,
                            String descricao, String local) {
        this.chaveEmpresa = empresa.getChave();
        this.empresa = empresa;
        this.inicio = inicio;
        this.termino = termino;
        this.descricao = descricao;
        this.local = local;
        this.ultimaAtualizacao = new Date();
        this.id = id;
    }
}
