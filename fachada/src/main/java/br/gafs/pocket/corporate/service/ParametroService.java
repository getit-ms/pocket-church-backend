/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import java.io.Serializable;

/**
 *
 * @author Gabriel
 */
public interface ParametroService extends Serializable {
    ParametrosGlobaisDTO buscaParametrosGlobais();
    ParametrosEmpresaDTO buscaParametros(String empresa);
    ConfiguracaoEmpresaDTO buscaConfiguracao(String empresa);
    ConfiguracaoYouTubeEmpresaDTO buscaConfiguracaoYouTube(String empresa);
    ConfiguracaoCalendarEmpresaDTO buscaConfiguracaoCalendar(String chaveEmpresa);
    
    void salvaParametros(ParametrosEmpresaDTO params, String empresa);
    void salvaConfiguracao(ConfiguracaoEmpresaDTO params, String empresa);
    void salvaConfiguracaoYouTube(ConfiguracaoYouTubeEmpresaDTO params, String empresa);
    void salvaConfiguracaoCalendar(ConfiguracaoCalendarEmpresaDTO params, String empresa);

    <T> T get(String grupo, TipoParametro param);

    <T> void set(String grupo, TipoParametro param, T value);

    ConfiguracaoFlickrEmpresaDTO buscaConfiguracaoFlickr(String empresa);
}
