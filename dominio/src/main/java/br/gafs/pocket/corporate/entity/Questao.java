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
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_questao")
public class Questao implements IEntity, Comparable<Questao> {
    @Id
    @Column(name = "id_questao")
    @SequenceGenerator(sequenceName = "seq_questao", name = "seq_questao")
    @GeneratedValue(generator = "seq_questao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 250)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "questao", length = 250, nullable = false)
    private String questao;

    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opcao> opcoes = new ArrayList<Opcao>();
    
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "id_enquete", referencedColumnName = "id_enquete", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false)
    })
    private Enquete enquete;
    
    public List<Opcao> getOpcoes(){
        Collections.sort(opcoes);
        return opcoes;
    }

    @Override
    public int compareTo(Questao o) {
        return id != null && o.id != null ? id.compareTo(o.id) : 0;
    }
    
}
