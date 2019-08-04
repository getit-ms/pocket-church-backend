package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "igreja")
@Table(name = "tb_template_aplicativo")
public class TemplateAplicativo implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_android_icon", referencedColumnName = "id_arquivo")
    })
    private Arquivo androidIcon;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_ios_icon", referencedColumnName = "id_arquivo")
    })
    private Arquivo iosIcon;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_splash", referencedColumnName = "id_arquivo")
    })
    private Arquivo splash;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_push_icon", referencedColumnName = "id_arquivo")
    })
    private Arquivo pushIcon;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_home", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundHome;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_home", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoHome;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_login", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundLogin;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_login", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoLogin;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_menu", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundMenu;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_menu", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoMenu;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_institucional", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundInstitucional;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_institucional", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoInstitucional;

    @ElementCollection
    @Column(name = "cor")
    @View.MergeViews(View.Resumido.class)
    @MapKeyColumn (name = "chave")
    @CollectionTable(name = "tb_cores_aplicativo",
            joinColumns = @JoinColumn(name="chave_igreja"))
    private Map<String, String> cores = new HashMap<String, String>();

    public TemplateAplicativo(Igreja igreja) {
        this.igreja = igreja;
    }

    @Override
    @JsonIgnore
    public Igreja getId() {
        return igreja;
    }
}
