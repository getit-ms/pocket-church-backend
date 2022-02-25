/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@IdClass(RespostaVotacaoId.class)
@Table(name = "tb_resposta_votacao")
@ToString(of = {"membro", "votacao"})
@EqualsAndHashCode(of = {"membro", "votacao"})
@NamedQueries({
        @NamedQuery(name = "RespostaVotacao.removerPorVotacao", query = "delete from RespostaVotacao rv where rv.votacao.id = :idVotacao and rv.igreja.chave = :chaveIgreja")
})
public class RespostaVotacao implements IEntity {
    @Id
    @Column(name = "id_resposta_votacao")
    @SequenceGenerator(name = "seq_resposta_votacao", sequenceName = "seq_resposta_votacao")
    @GeneratedValue(generator = "seq_resposta_votacao", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "id_votacao", insertable = false, updatable = false)
    private Long idVotacao;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Setter
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_votacao", referencedColumnName = "id_votacao", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false)
    })
    private Votacao votacao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data", nullable = false)
    private Date data = DateUtil.getDataAtual();

    @OneToMany(mappedBy = "votacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaQuestao> respostas = new ArrayList<RespostaQuestao>();

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", insertable = false, updatable = false)
    private Igreja igreja;

    public RespostaVotacao(Votacao votacao) {
        this.votacao = votacao;
        for (Questao questao : votacao.getQuestoes()) {
            respostas.add(new RespostaQuestao(this, questao));
        }
    }

    @Override
    public RespostaVotacaoId getId() {
        return new RespostaVotacaoId(id,
                new RegistroIgrejaId(votacao.getIgreja().getChave(), votacao.getId()));
    }
}
