package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusItemEvento;
import br.gafs.calvinista.entity.domain.TipoItemEvento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ItemEventoId.class)
@Table(name = "tb_item_evento")
@EqualsAndHashCode(of = {"id", "chaveIgreja", "tipo"})
@NamedQueries({
        @NamedQuery(name = "ItemEvento.findByPeriodoAndTipos", query = "select ie, null, 0, 0 from ItemEvento ie where ie.igreja.chave = :chaveIgreja and ie.status = :status and ie.dataHoraReferencia between :dataInicio and :dataTermino and ie.tipo in :tipos order by ie.dataHoraReferencia, ie.tipo, ie.id"),
        @NamedQuery(name = "ItemEvento.findByPeriodo", query = "select ie, (select c.dataHora from CurtidaItemEvento c where c.itemEvento = ie and c.membro.id = :membro), (select count(c) from CurtidaItemEvento c where c.itemEvento = ie), (select count(c) from ComentarioItemEvento c where c.itemEvento = ie and c.status = :statusComentario) from ItemEvento ie where ie.igreja.chave = :chaveIgreja and ie.status = :status and ie.dataHoraReferencia between :dataInicio and :dataTermino order by ie.dataHoraReferencia, ie.tipo, ie.id"),
        @NamedQuery(name = "ItemEvento.updateNaoPublicadosByIgrejaAndTipo", query = "update ItemEvento ie set ie.status = :statusPublicado where ie.igreja.chave = :igreja and ie.tipo = :tipo and ie.status = :statusNaoPublicado and ie.dataHoraPublicacao <= :data"),
})
public class ItemEvento implements IEntity {
    @Id
    @Column(name = "id_item_evento")
    private String id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Id
    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private TipoItemEvento tipo;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "apresentacao")
    private String apresentacao;

    @Column(name = "data_hora_publicacao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraPublicacao;

    @Column(name = "data_hora_referencia")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraReferencia;

    @JsonIgnore
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusItemEvento status;

    @Column(name = "url_ilustracao")
    private String urlIlustracao;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_ilustracao", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Arquivo ilustracao;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_autor", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Membro autor;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Setter
    @Transient
    private boolean curtido;

    @Setter
    @Transient
    private Integer quantidadeCurtidas;

    @Setter
    @Transient
    private Integer quantidadeComentarios;

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;

        if (igreja != null) {
            this.chaveIgreja = igreja.getChave();
        } else {
            this.chaveIgreja = null;
        }
    }
}
