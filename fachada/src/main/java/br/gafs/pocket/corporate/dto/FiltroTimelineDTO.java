package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FiltroTimelineDTO implements DTO {
    private Integer pagina = 1;
    private Integer total = 10;
}
