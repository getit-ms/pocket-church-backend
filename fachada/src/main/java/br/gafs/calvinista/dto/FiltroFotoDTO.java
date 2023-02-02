package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Gabriel on 24/07/2018.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FiltroFotoDTO implements DTO {
    private String galeria;
    private Integer pagina;
}
