/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.Votacao;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel
 */
@Getter
@NoArgsConstructor
public class ResultadoVotacaoDTO implements DTO {
    @JsonIgnore
    private Igreja igreja;
    private List<ResultadoQuestaoDTO> questoes = new ArrayList<ResultadoQuestaoDTO>();
    private Long id;
    private String nome;
    private String descricao;

    public ResultadoVotacaoDTO(Votacao votacao) {
        this.igreja = votacao.getIgreja();
        this.id = votacao.getId();
        this.nome = votacao.getNome();
        this.descricao = votacao.getDescricao();
    }

    public ResultadoQuestaoDTO init(Questao questao) {
        ResultadoQuestaoDTO dto = new ResultadoQuestaoDTO(questao);
        questoes.add(dto);
        return dto;
    }
}
