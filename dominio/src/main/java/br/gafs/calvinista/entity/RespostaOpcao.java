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

/**
 * @author Gabriel
 */
@Entity
@Getter
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_resposta_opcao")
@NamedQueries({
        @NamedQuery(name = "RespostaOpcao.removerPorVotacao", query = "delete from RespostaOpcao ro where ro.questao.id in (select rq.id from RespostaQuestao rq where rq.votacao.votacao.id = :idVotacao and rq.votacao.igreja.chave = :chaveIgreja)")
})
public class RespostaOpcao implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_resposta_opcao")
    @SequenceGenerator(name = "seq_resposta_opcao", sequenceName = "seq_resposta_opcao")
    @GeneratedValue(generator = "seq_resposta_opcao", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_resposta_questao")
    private RespostaQuestao questao;

    @Setter
    @ManyToOne
    @JoinColumn(name = "id_opcao")
    private Opcao opcao;

}
