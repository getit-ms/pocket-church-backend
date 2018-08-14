package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
import br.gafs.util.string.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoInscricaoDTO implements DTO {
    private String checkoutPagSeguro;

    public boolean isDevePagar(){
        return !StringUtil.isEmpty(checkoutPagSeguro);
    }
}
