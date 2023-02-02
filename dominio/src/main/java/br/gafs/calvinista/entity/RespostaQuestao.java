/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@Table(name = "tb_resposta_questao")
@ToString(of = {"votacao", "questao"})
@EqualsAndHashCode(of = {"votacao", "questao"})
@NamedQueries({
        @NamedQuery(name = "RespostaQuestao.removerPorVotacao", query = "delete from RespostaQuestao rq where rq.votacao.votacao.id = :idVotacao and rq.votacao.igreja.chave = :chaveIgreja"),
        @NamedQuery(name = "RespostaQuestao.findCountByOpcao", query = "select count(ro.id) from RespostaQuestao rq inner join rq.opcoes ro where ro.opcao.id = :opcao"),
        @NamedQuery(name = "RespostaQuestao.findCountNulos", query = "select sum(rq.nulos) from RespostaQuestao rq where rq.questao.id = :questao"),
        @NamedQuery(name = "RespostaQuestao.findCountBrancos", query = "select sum(rq.brancos) from RespostaQuestao rq where rq.questao.id = :questao")
})
public class RespostaQuestao implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_resposta_questao")
    @SequenceGenerator(sequenceName = "seq_resposta_questao", name = "seq_resposta_questao")
    @GeneratedValue(generator = "seq_resposta_questao", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_resposta_votacao", referencedColumnName = "id_resposta_votacao"),
            @JoinColumn(name = "id_votacao", referencedColumnName = "id_votacao"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private RespostaVotacao votacao;

    @Setter
    @ManyToOne
    @JoinColumn(name = "id_questao")
    private Questao questao;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_resposta_questao")
    private List<RespostaOpcao> opcoes = new ArrayList<RespostaOpcao>();

    @Min(0)
    @Column(name = "branco")
    private Integer brancos = 0;

    @Min(0)
    @Column(name = "nulo")
    private Integer nulos = 0;

    public RespostaQuestao(RespostaVotacao votacao, Questao questao) {
        this.votacao = votacao;
        this.questao = questao;
    }

    public Integer getQuantidadeVotos() {
        return brancos + nulos + opcoes.size();
    }

}
