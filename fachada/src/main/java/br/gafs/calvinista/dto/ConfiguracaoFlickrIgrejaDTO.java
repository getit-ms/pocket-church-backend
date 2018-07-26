/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.util.string.StringUtil;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ConfiguracaoFlickrIgrejaDTO {
    @TipoParametro.Mapping(TipoParametro.FLICKR_ID)
    private String idFlickr;
    
    public boolean isConfigurado(){
        return !StringUtil.isEmpty(idFlickr);
    }
    
}
