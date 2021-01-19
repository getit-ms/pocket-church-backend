/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.geracao;

import br.gafs.pocket.corporate.dto.ParametrosEmpresaDTO;
import br.gafs.pocket.corporate.entity.Empresa;

import java.io.File;
import java.io.OutputStream;

/**
 *
 * @author Gabriel
 */
public interface EtapaProcessamento {
    boolean started();
    boolean ended();
    File execute(File source, Empresa empresa, ParametrosEmpresaDTO parmas, OutputStream console) throws Exception ;
    int process();
    void kill();
}
