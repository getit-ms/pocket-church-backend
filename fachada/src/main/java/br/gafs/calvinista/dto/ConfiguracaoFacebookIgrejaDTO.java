/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ConfiguracaoFacebookIgrejaDTO {
    @JsonIgnore
    @TipoParametro.Mapping(TipoParametro.FACEBOOK_APP_CODE)
    private String code;
    
    @TipoParametro.Mapping(TipoParametro.FACEBOOK_PAGE_ID)
    private String pagina;

    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_FACEBOOK_AO_VIVO)
    private String tituloAoVivo;
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_FACEBOOK_AO_VIVO)
    private String textoAoVivo;

    @TipoParametro.Mapping(TipoParametro.PUSH_TITLE_FACEBOOK_AGENDADO)
    private String tituloAgendado;
    @TipoParametro.Mapping(TipoParametro.PUSH_BODY_FACEBOOK_AGENDADO)
    private String textoAgendado;

    public boolean isConfigurado(){
        return !StringUtil.isEmpty(code);
    }
    
}
