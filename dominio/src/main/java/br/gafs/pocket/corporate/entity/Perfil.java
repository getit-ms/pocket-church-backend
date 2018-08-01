/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_perfil")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@NamedQueries({
    @NamedQuery(name = "Perfil.findByEmpresa", query = "select p from Perfil p where p.empresa.chave = :idEmpresa order by p.nome")
})
public class Perfil implements IEntity {
    @Id
    @Column(name = "id_perfil")
    @SequenceGenerator(name = "seq_perfil", sequenceName = "seq_perfil")
    @GeneratedValue(generator = "seq_perfil", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionalidade")
    @View.MergeViews(View.Edicao.class)
    @CollectionTable(name = "rl_perfil_funcionalidade",
            joinColumns = {
                @JoinColumn(name = "id_perfil", referencedColumnName = "id_perfil"),
                @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
            })
    private List<Funcionalidade> funcionalidades = new ArrayList<Funcionalidade>();
    
    @JsonIgnore
    @ManyToMany(mappedBy = "perfis")
    private List<Acesso> acessos;

    public Perfil(Empresa empresa) {
        this.empresa = empresa;
    }

    public boolean isExigeMinisterios() {
        for (Funcionalidade f : funcionalidades){
            if (f.isAssociaMinisterios()){
                return true;
            }
        }
        return false;
    }
    
}
