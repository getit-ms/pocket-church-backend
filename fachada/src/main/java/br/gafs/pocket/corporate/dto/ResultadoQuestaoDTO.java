package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.Opcao;
import br.gafs.pocket.corporate.entity.Questao;
import br.gafs.dto.DTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Gabriel on 21/05/2018.
 */
@Getter
@NoArgsConstructor
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
