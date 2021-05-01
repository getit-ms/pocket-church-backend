package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.ws.rs.QueryParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroIgrejaDTO implements DTO {
    private String chave;
    private String filtro;
    private String agrupamento;
    private Integer pagina = 1;
    private Integer total = 50;
}
