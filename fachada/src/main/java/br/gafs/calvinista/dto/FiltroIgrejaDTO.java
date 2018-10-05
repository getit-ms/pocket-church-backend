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
    @QueryParam("chave")
    private String chave;

    @QueryParam("filtro")
    private String filtro;

    @QueryParam("pagina")
    private Integer pagina = 1;

    @QueryParam("total")
    private Integer total = 50;
}
