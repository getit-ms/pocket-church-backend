package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoItemEvento;
import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FiltroComentarioDTO implements DTO {
    private String idItemEvento;
    private TipoItemEvento tipoItemEvento;
    private Integer pagina;
    private Integer total;
}
