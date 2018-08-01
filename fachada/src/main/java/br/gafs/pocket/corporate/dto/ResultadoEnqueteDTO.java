/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.Enquete;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Questao;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Getter
@NoArgsConstructor
public class ResultadoEnqueteDTO implements DTO {
    @JsonIgnore
    private Empresa empresa;
    private List<ResultadoQuestaoDTO> questoes = new ArrayList<ResultadoQuestaoDTO>();
    private Long id;
    private String nome;
    private String descricao;

    public ResultadoEnqueteDTO(Enquete enquete) {
        this.empresa = enquete.getEmpresa();
        this.id = enquete.getId();
        this.nome = enquete.getNome();
        this.descricao = enquete.getDescricao();
    }
    
    public ResultadoQuestaoDTO init(Questao questao){
        ResultadoQuestaoDTO dto = new ResultadoQuestaoDTO(questao);
        questoes.add(dto);
        return dto;
    }
}
