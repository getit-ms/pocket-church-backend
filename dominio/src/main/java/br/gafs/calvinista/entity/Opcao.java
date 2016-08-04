/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Table(name = "tb_opcao")
@EqualsAndHashCode(of = "id")
public class Opcao implements IEntity, Comparable<Opcao> {
    @Id
    @Column(name = "id_opcao")
    @SequenceGenerator(sequenceName = "seq_opcao", name = "seq_opcao")
    @GeneratedValue(generator = "seq_opcao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 250)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "opcao", length = 250, nullable = false)
    private String opcao;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_questao", nullable = false)
    private Questao questao;

    @Override
    public int compareTo(Opcao o) {
        return id != null && o.id != null ? id.compareTo(o.id) : 0;
    }
    
    
}
