/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
@IdClass(RegistroIgrejaId.class)
@NamedQueries({
    @NamedQuery(name = "Perfil.findByIgreja", query = "select p from Perfil p where p.igreja.chave = :idIgreja order by p.nome")
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
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionalidade")
    @View.MergeViews(View.Edicao.class)
    @CollectionTable(name = "rl_perfil_funcionalidade",
            joinColumns = {
                @JoinColumn(name = "id_perfil", referencedColumnName = "id_perfil"),
                @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
            })
    private List<Funcionalidade> funcionalidades = new ArrayList<Funcionalidade>();
    
    @JsonIgnore
    @ManyToMany(mappedBy = "perfis")
    private List<Acesso> acessos;

    public Perfil(Igreja igreja) {
        this.igreja = igreja;
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
