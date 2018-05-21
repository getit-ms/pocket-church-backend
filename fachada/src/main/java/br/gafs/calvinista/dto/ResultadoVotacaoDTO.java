/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Opcao;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.Votacao;
import br.gafs.dto.DTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Getter
@NoArgsConstructor
public class ResultadoVotacaoDTO implements DTO {
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
    
    public ResultadoQuestaoDTO init(Questao questao){
        ResultadoQuestaoDTO dto = new ResultadoQuestaoDTO(questao);
        questoes.add(dto);
        return dto;
    }

    @Getter
    @NoArgsConstructor
    public static class ResultadoQuestaoDTO implements DTO {
        private List<ResultadoOpcaoDTO> validos = new ArrayList<ResultadoOpcaoDTO>();
        private List<ResultadoOpcaoDTO> totais = new ArrayList<ResultadoOpcaoDTO>();

        @Setter
        private String questao;

        public ResultadoQuestaoDTO(Questao questao) {
            this.questao = questao.getQuestao();
        }
        
        public ResultadoQuestaoDTO resultado(Opcao opcao, int resultado){
            validos.add(new ResultadoOpcaoDTO(opcao, resultado));
            totais.add(new ResultadoOpcaoDTO(opcao, resultado));
            Collections.sort(validos);
            Collections.sort(totais);
            return this;
        }
        
        public ResultadoQuestaoDTO brancos(int resultado){
            totais.add(new ResultadoOpcaoDTO("Brancos", resultado));
            Collections.sort(totais);
            return this;
        }
        
        public ResultadoQuestaoDTO nulos(int resultado){
            totais.add(new ResultadoOpcaoDTO("Nulos", resultado));
            Collections.sort(totais);
            return this;
        }
        
        public int getTotalTotais(){
            int total = 0;
            for (ResultadoOpcaoDTO opcao : totais){
                total += opcao.getResultado();
            }
            return total;
        }
        
        public int getTotalValidos(){
            int total = 0;
            for (ResultadoOpcaoDTO opcao : validos){
                total += opcao.getResultado();
            }
            return total;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ResultadoOpcaoDTO implements DTO, Comparable<ResultadoOpcaoDTO> {
        private String opcao;
        private Integer resultado;

        public ResultadoOpcaoDTO(String opcao, int resultado) {
            this.opcao = opcao;
            this.resultado = resultado;
        }

        public ResultadoOpcaoDTO(Opcao opcao, int resultado) {
            this(opcao.getOpcao(), resultado);
        }

        @Override
        public int compareTo(ResultadoOpcaoDTO o) {
            return o.resultado - resultado;
        }
    }
}
