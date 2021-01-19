/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "empresa")
@EqualsAndHashCode(of = "empresa")
@Table(name = "tb_institucional")
public class Institucional implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;
    
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
    @MapKeyColumn (name = "rede_social")
    @CollectionTable(name = "tb_redes_sociais",
            joinColumns = @JoinColumn(name="chave_empresa"))
    private Map<String, String> redesSociais = new HashMap<String, String>();
    
    @OneToOne
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
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
    @CollectionTable(name = "rl_telefone_empresa",
            joinColumns = @JoinColumn(name = "chave_empresa"))
    private List<String> telefones = new ArrayList<String>();
    
    @NotNull
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(name = "rl_endereco_empresa",
            joinColumns = @JoinColumn(name = "chave_empresa"),
            inverseJoinColumns = @JoinColumn(name = "id_endereco", nullable = false))
    private List<Endereco> enderecos = new ArrayList(Arrays.asList(new Endereco()));

    public Institucional(Empresa empresa) {
        this.empresa = empresa;
    }
    
    public void addTelefone(String telefone){
        telefones.add(telefone);
    }
    
    public void removeTelefone(String telefone){
        telefones.remove(telefone);
    }
    
    public Endereco getEndereco(){
        if (enderecos.size() == 1){
            return enderecos.get(0);
        }
        return null;
    }
    
    public void setEndereco(Endereco endereco){
        enderecos.clear();
        enderecos.add(endereco);
    }
    
    @JsonIgnore
    public Empresa getId(){
        return empresa;
    }
}
