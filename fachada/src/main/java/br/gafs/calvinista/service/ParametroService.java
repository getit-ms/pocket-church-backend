/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.ParametrosGlobaisDTO;
import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.TipoParametro;
import java.io.Serializable;

/**
 *
 * @author Gabriel
 */
public interface ParametroService extends Serializable {
    public ParametrosGlobaisDTO buscaParametrosGlobais();
    public ParametrosIgrejaDTO buscaParametros(Igreja igreja);
    
    public void salvaParametrosGlobais(ParametrosGlobaisDTO params);
    public void salvaParametros(ParametrosIgrejaDTO params, Igreja igreja);
    
    <T> T get(String grupo, TipoParametro param);
    <T> void set(String grupo, TipoParametro param, T value);
}
