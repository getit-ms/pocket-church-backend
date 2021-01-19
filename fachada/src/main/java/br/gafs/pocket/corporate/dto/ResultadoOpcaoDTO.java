package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.Opcao;
import br.gafs.dto.DTO;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Gabriel on 21/05/2018.
 */

@Data
@NoArgsConstructor
public class ResultadoOpcaoDTO implements DTO, Comparable<ResultadoOpcaoDTO> {
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
