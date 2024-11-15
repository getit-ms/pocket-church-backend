package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoCifra;
import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroCifraDTO implements DTO {
    private String filtro;
    private TipoCifra tipo = TipoCifra.CIFRA;
    private Integer pagina = 1;
    private Integer total = 50;
}
