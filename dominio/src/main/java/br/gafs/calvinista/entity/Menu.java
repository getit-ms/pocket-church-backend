package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
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
@IdClass(RegistroIgrejaId.class)
@EqualsAndHashCode(of = {"id", "chave_igreja"})
@NamedQueries({
        @NamedQuery(name = "Menu.findByIgrejaAndFuncionalidades", query = "select m from Menu m left join m.menuPai mp where m.funcionalidade in :funcionalidades and m.igreja.chave = :igreja order by mp.chaveIgreja desc, m.ordem, mp.ordem")
})
public class Menu implements IEntity {
    @Id
    @Column(name = "id_menu")
    @SequenceGenerator(name = "seq_menu", sequenceName = "seq_menu")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_menu")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @ManyToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

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
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Menu menuPai;
}
