/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.util.string.StringUtil;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ConfiguracaoYouTubeEmpresaDTO {
    @TipoParametro.Mapping(TipoParametro.YOUTUBE_CHANNEL_ID)
    private String idCanal;
    
    @TipoParametro.Mapping(TipoParametro.TITULO_YOUTUBE_AO_VIVO)
    private String tituloAoVivo;
    @TipoParametro.Mapping(TipoParametro.TEXTO_YOUTUBE_AO_VIVO)
    private String textoAoVivo;
    
    @TipoParametro.Mapping(TipoParametro.TITULO_YOUTUBE_AGENDADO)
    private String tituloAgendado;
    @TipoParametro.Mapping(TipoParametro.TEXTO_YOUTUBE_AGENDADO)
    private String textoAgendado;
    
    public boolean isConfigurado(){
        return !StringUtil.isEmpty(idCanal);
    }
    
}
