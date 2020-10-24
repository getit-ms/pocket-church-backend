package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
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
@EqualsAndHashCode(of = {"id", "chaveEmpresa", "tipo"})
@NamedQueries({
        @NamedQuery(name = "ItemEvento.findByPeriodo", query = "select ie, (select c from CurtidaItemEvento c where c.itemEvento = ie and c.colaborador = :colaborador), (select count(c) from CurtidaItemEvento c where c.itemEvento = ie), (select count(c) from ComentarioItemEvento c where c.itemEvento = ie and c.status = :statusComentario) from ItemEvento ie where ie.empresa.chave = :chaveEmpresa and ie.status = :status and ie.dataHoraReferencia between :dataInicio and :dataTermino order by ie.dataHoraReferencia, ie.tipo, ie.id"),
})
public class ItemEvento implements IEntity {
    @Id
    @Column(name = "id_item_evento")
    private String id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

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
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Arquivo ilustracao;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_autor", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Colaborador autor;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    @Setter
    @Transient
    private boolean curtido;

    @Setter
    @Transient
    private Integer quantidadeCurtidas;

    @Setter
    @Transient
    private Integer quantidadeComentarios;

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;

        if (empresa != null) {
            this.chaveEmpresa = empresa.getChave();
        } else {
            this.chaveEmpresa = null;
        }
    }
}
