package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "empresa")
@EqualsAndHashCode(of = "empresa")
@Table(name = "tb_template_empresa")
public class Template implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    @Column(name = "cor_principal")
    private String corPrincipal;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_pequena", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoPequena;

    @OneToOne
    @View.MergeViews(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_grande", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoGrande;

    @OneToOne
    @View.MergeViews(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_banner", referencedColumnName = "id_arquivo")
    })
    private Arquivo banner;

    @OneToOne
    @View.MergeViews(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_report", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoReports;

    public Template(Empresa empresa) {
        this.empresa = empresa;
    }

    @JsonIgnore
    public Empresa getId(){
        return empresa;
    }
}
