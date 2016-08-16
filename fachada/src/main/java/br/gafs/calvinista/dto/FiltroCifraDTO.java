package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroCifraDTO implements DTO {
    private String filtro;
    private Integer pagina = 1;
    private Integer total = 50;
}
