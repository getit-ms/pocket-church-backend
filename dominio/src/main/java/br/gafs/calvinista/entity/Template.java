/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "igreja")
@EqualsAndHashCode(of = "igreja")
@Table(name = "tb_template_igreja")
public class Template implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Column(name = "cor_principal")
    private String corPrincipal;

    @OneToOne
    @View.MergeViews(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_pequena", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoPequena;

    @OneToOne
    @View.MergeViews(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_grande", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoGrande;

    @OneToOne
    @View.MergeViews(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_banner", referencedColumnName = "id_arquivo")
    })
    private Arquivo banner;

    @OneToOne
    @View.MergeViews(View.Detalhado.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_logo_report", referencedColumnName = "id_arquivo")
    })
    private Arquivo logoReports;

    public Template(Igreja igreja) {
        this.igreja = igreja;
    }

    @JsonIgnore
    public Igreja getId() {
        return igreja;
    }
}
