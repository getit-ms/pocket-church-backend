/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Opcao;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.RespostaQuestao;
import br.gafs.calvinista.entity.Votacao;
import br.gafs.dto.DTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Gabriel
 */
@Getter
public class ResultadoVotacaoDTO implements DTO {
    private List<ResultadoQuestaoDTO> questoes = new ArrayList<ResultadoQuestaoDTO>();
    private String nome;
    private String descricao;

    public ResultadoVotacaoDTO(Votacao votacao) {
        this.nome = votacao.getNome();
        this.descricao = votacao.getDescricao();
    }
    
    public ResultadoQuestaoDTO init(Questao questao){
        ResultadoQuestaoDTO dto = new ResultadoQuestaoDTO(questao);
        questoes.add(dto);
        return dto;
    }
    
    @Getter
    public class ResultadoQuestaoDTO implements DTO {
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
            return this;
        }
        
        public ResultadoQuestaoDTO brancos(int resultado){
            totais.add(new ResultadoOpcaoDTO("Brancos", resultado));
            return this;
        }
        
        public ResultadoQuestaoDTO nulos(int resultado){
            totais.add(new ResultadoOpcaoDTO("Nulos", resultado));
            return this;
        }
        
        public int getQuantidadeTotais(){
            int total = 0;
            for (ResultadoOpcaoDTO opcao : totais){
                total += opcao.getResultado();
            }
            return total;
        }
        
        public int getQuantidadezValidos(){
            int total = 0;
            for (ResultadoOpcaoDTO opcao : validos){
                total += opcao.getResultado();
            }
            return total;
        }
    }

    @Data
    public class ResultadoOpcaoDTO implements DTO {
        private String opcao;
        private int resultado;

        public ResultadoOpcaoDTO(String opcao, int resultado) {
            this.opcao = opcao;
            this.resultado = resultado;
        }

        public ResultadoOpcaoDTO(Opcao opcao, int resultado) {
            this(opcao.getOpcao(), resultado);
        }
        
    }
}
