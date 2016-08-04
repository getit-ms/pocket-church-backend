/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.geracao;

import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.entity.Igreja;
import java.io.File;
import java.io.OutputStream;

/**
 *
 * @author Gabriel
 */
public interface EtapaProcessamento {
    boolean started();
    boolean ended();
    File execute(File source, Igreja igreja, ParametrosIgrejaDTO parmas, OutputStream console) throws Exception ;
    int process();
    void kill();
}
