package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import lombok.Getter;

@Getter
public class FiltroTimelineDTO implements DTO {
    private Integer pagina = 1;
    private Integer total = 10;
}
