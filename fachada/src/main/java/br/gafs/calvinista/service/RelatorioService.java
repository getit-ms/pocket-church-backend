package br.gafs.calvinista.service;

import br.gafs.calvinista.entity.domain.TipoEvento;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by mirante0 on 01/02/2017.
 */
public interface RelatorioService extends Serializable {
    File exportaInscritos(Long evento, String tipo) throws IOException, InterruptedException;
    File exportaInscritos(TipoEvento tipo) throws IOException, InterruptedException;
    File exportaHino(Long hino, String tipo) throws IOException, InterruptedException;
    File exportaEstudo(Long estudo, String tipo) throws IOException, InterruptedException;
}
