package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.view.View;
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
@IdClass(RegistroEmpresaId.class)
@EqualsAndHashCode(of = {"id", "empresa"})
public class Banner implements IEntity {
    @Id
    @Column(name = "id_banner")
    @SequenceGenerator(sequenceName = "seq_banner", name = "seq_banner")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_banner")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

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
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
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
