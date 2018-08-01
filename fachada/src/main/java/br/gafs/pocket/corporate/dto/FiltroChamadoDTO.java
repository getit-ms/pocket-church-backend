package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroChamadoDTO implements DTO{
    private Integer pagina;
    private Integer total;
}
