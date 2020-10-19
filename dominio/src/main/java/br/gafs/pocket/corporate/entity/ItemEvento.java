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
        @NamedQuery(name = "ItemEvento.findByPeriodo", query = "select ie from ItemEvento ie where ie.empresa.chave = :chaveEmpresa and ie.status = :status and ie.dataHora between :dataInicio and :dataTermino order by ie.dataHora, ie.tipo, ie.id")
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

    @Column(name = "data_hora")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora;

    @JsonIgnore
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusItemEvento status;

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

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;

        if (empresa != null) {
            this.chaveEmpresa = empresa.getChave();
        } else {
            this.chaveEmpresa = null;
        }
    }
}
