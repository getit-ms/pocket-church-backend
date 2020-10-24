package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
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
