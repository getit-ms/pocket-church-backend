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
public class ConfiguracaoYouTubeIgrejaDTO {
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
