package br.gafs.pocket.corporate.dto;

import br.gafs.util.string.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by Gabriel on 18/02/2018.
 */
@Getter
@AllArgsConstructor
public class BuscaPaginadaEventosCalendarioDTO {
    private List<EventoCalendarioDTO> eventos;
    private String proximaPagina;

    public boolean isPossuiProximaPagina() {
        return !StringUtil.isEmpty(proximaPagina);
    }
}
