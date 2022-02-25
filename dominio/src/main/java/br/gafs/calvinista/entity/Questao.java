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
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
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

    @Min(1)
    @NotNull
    @View.MergeViews(View.Edicao.class)
    @Column(name = "quantidade_votos", nullable = false)
    private Integer quantidadeVotos = 1;

    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opcao> opcoes = new ArrayList<Opcao>();

    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_votacao", referencedColumnName = "id_votacao", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false)
    })
    private Votacao votacao;

    public List<Opcao> getOpcoes() {
        Collections.sort(opcoes);
        return opcoes;
    }

    @Override
    public int compareTo(Questao o) {
        return id != null && o.id != null ? id.compareTo(o.id) : 0;
    }

}
