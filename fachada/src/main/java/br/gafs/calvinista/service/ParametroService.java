/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.domain.TipoParametro;
import java.io.Serializable;

/**
 *
 * @author Gabriel
 */
public interface ParametroService extends Serializable {
    ParametrosGlobaisDTO buscaParametrosGlobais();
    ParametrosIgrejaDTO buscaParametros(String igreja);
    ConfiguracaoIgrejaDTO buscaConfiguracao(String igreja);
    ConfiguracaoYouTubeIgrejaDTO buscaConfiguracaoYouTube(String igreja);
    ConfiguracaoCalendarIgrejaDTO buscaConfiguracaoCalendar(String chaveIgreja);
    
    void salvaParametrosGlobais(ParametrosGlobaisDTO params);
    void salvaParametros(ParametrosIgrejaDTO params, String igreja);
    void salvaConfiguracao(ConfiguracaoIgrejaDTO params, String igreja);
    void salvaConfiguracaoYouTube(ConfiguracaoYouTubeIgrejaDTO params, String igreja);
    void salvaConfiguracaoCalendar(ConfiguracaoCalendarIgrejaDTO params, String igreja);

    <T> T get(String grupo, TipoParametro param);

    <T> void set(String grupo, TipoParametro param, T value);
}
