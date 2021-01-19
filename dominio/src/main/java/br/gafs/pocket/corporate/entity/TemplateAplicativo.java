package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(of = "empresa")
@EqualsAndHashCode(of = "empresa")
@Table(name = "tb_template_aplicativo")
public class TemplateAplicativo implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_android_icon", referencedColumnName = "id_arquivo")
    })
    private Arquivo androidIcon;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_ios_icon", referencedColumnName = "id_arquivo")
    })
    private Arquivo iosIcon;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_splash", referencedColumnName = "id_arquivo")
    })
    private Arquivo splash;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_push_icon", referencedColumnName = "id_arquivo")
    })
    private Arquivo pushIcon;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_home", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundHome;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_home", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoHome;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_login", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundLogin;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_login", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoLogin;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_menu", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundMenu;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_menu", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoMenu;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_background_institucional", referencedColumnName = "id_arquivo")
    })
    private Arquivo backgroundInstitucional;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_institucional", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoInstitucional;

    @ElementCollection
    @Column(name = "cor")
    @View.MergeViews(View.Resumido.class)
    @MapKeyColumn (name = "chave")
    @CollectionTable(name = "tb_cores_aplicativo",
            joinColumns = @JoinColumn(name="chave_empresa"))
    private Map<String, String> cores = new HashMap<String, String>();

    public TemplateAplicativo(Empresa empresa) {
        this.empresa = empresa;
    }

    @Override
    @JsonIgnore
    public Empresa getId() {
        return empresa;
    }
}
