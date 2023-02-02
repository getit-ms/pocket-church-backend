package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Entity
@Table(name = "tb_banner")
@IdClass(RegistroIgrejaId.class)
@EqualsAndHashCode(of = {"id", "igreja"})
@NamedQueries({
        @NamedQuery(name = "Bannder.findByIgreja", query = "select b from Banner b where b.igreja.chave = :igreja order by b.ordem, b.id")
})
public class Banner implements IEntity {
    @Id
    @Column(name = "id_banner")
    @SequenceGenerator(sequenceName = "seq_banner", name = "seq_banner")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_banner")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @NotNull
    @Column(name = "ordem")
    @View.MergeViews({View.Edicao.class, View.Cadastro.class})
    private Integer ordem;

    @Setter
    @NotNull
    @ManyToOne
    @View.MergeViews({View.Edicao.class, View.Cadastro.class})
    @JoinColumns({
            @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Arquivo banner;

    @Length(max = 500)
    @Column(name = "link_externo")
    @View.MergeViews({View.Edicao.class, View.Cadastro.class})
    private String linkExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcionalidade")
    @View.MergeViews({View.Edicao.class, View.Cadastro.class})
    private Funcionalidade funcionalidade;

    @Column(name = "referencia_interna")
    @View.MergeViews({View.Edicao.class, View.Cadastro.class})
    private String referenciaInterna;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;
        if (igreja != null) {
            this.chaveIgreja = igreja.getChave();
        } else {
            this.chaveIgreja = null;
        }
    }
}
