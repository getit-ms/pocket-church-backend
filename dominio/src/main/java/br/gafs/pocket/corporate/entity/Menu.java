package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

/**
 * Created by Gabriel on 22/02/2018.
 */
@Getter
@Entity
@Table(name = "tb_menu")
@IdClass(RegistroEmpresaId.class)
@EqualsAndHashCode(of = {"id", "chave_empresa"})
@NamedQueries({
        @NamedQuery(name = "Menu.findByEmpresaAndFuncionalidades", query = "select m from Menu m left join m.menuPai mp where m.funcionalidade in :funcionalidades and m.empresa.chave = :empresa")
})
public class Menu implements IEntity {
    @Id
    @Column(name = "id_menu")
    @SequenceGenerator(name = "seq_menu", sequenceName = "seq_menu")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_menu")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @ManyToOne
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    @Column(name = "nome")
    private String nome;

    @Column(name = "icone")
    private String icone;

    @Column(name = "link")
    private String link;

    @Column(name = "ordem")
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcionalidade")
    private Funcionalidade funcionalidade;

    @JoinColumns({
            @JoinColumn(name = "id_menu_pai", referencedColumnName = "id_menu"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Menu menuPai;
}
