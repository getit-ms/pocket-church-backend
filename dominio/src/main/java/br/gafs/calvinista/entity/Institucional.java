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
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "igreja")
@EqualsAndHashCode(of = "igreja")
@Table(name = "tb_institucional")
public class Institucional implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Email
    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "email", length = 150)
    private String email;

    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "site", length = 150)
    private String site;

    @ElementCollection
    @Column(name = "url")
    @View.MergeViews(View.Edicao.class)
    @MapKeyColumn(name = "rede_social")
    @CollectionTable(name = "tb_redes_sociais",
            joinColumns = @JoinColumn(name = "chave_igreja"))
    private Map<String, String> redesSociais = new HashMap<String, String>();

    @OneToOne
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
            @JoinColumn(name = "id_divulgacao", referencedColumnName = "id_arquivo")
    })
    private Arquivo divulgacao;

    @View.MergeViews(View.Edicao.class)
    @Column(name = "texto_divulgacao")
    private String textoDivulgacao;

    @View.MergeViews(View.Edicao.class)
    @Column(name = "quem_somos", columnDefinition = "TEXT")
    private String quemSomos;

    @ElementCollection
    @Column(name = "telefone")
    @View.MergeViews(View.Edicao.class)
    @CollectionTable(name = "rl_telefone_igreja",
            joinColumns = @JoinColumn(name = "chave_igreja"))
    private List<String> telefones = new ArrayList<String>();

    @NotNull
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "rl_endereco_igreja",
            joinColumns = @JoinColumn(name = "chave_igreja"),
            inverseJoinColumns = @JoinColumn(name = "id_endereco", nullable = false))
    private List<Endereco> enderecos = new ArrayList(Arrays.asList(new Endereco()));

    public Institucional(Igreja igreja) {
        this.igreja = igreja;
    }

    public void addTelefone(String telefone) {
        telefones.add(telefone);
    }

    public void removeTelefone(String telefone) {
        telefones.remove(telefone);
    }

    public Endereco getEndereco() {
        if (enderecos.size() == 1) {
            return enderecos.get(0);
        }
        return null;
    }

    public void setEndereco(Endereco endereco) {
        enderecos.clear();
        enderecos.add(endereco);
    }

    @JsonIgnore
    public Igreja getId() {
        return igreja;
    }
}
