/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.util.string.StringUtil;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Gabriel
 */
@Data
public class ConfiguracaoCalendarIgrejaDTO {
    @TipoParametro.Mapping(TipoParametro.GOOGLE_CALENDAR_ID)
    private List<String> idCalendario;
    
    public boolean isConfigurado(){
        return idCalendario != null && !idCalendario.isEmpty();
    }
    
}
