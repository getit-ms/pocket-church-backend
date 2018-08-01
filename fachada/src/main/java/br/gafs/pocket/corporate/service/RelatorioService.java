package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.entity.domain.TipoEvento;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by mirante0 on 01/02/2017.
 */
public interface RelatorioService extends Serializable {
    File exportaInscritos(Long evento, String tipo) throws IOException, InterruptedException;
    File exportaInscritos(TipoEvento tipo) throws IOException, InterruptedException;
    File exportaContatos() throws IOException, InterruptedException;
    File exportaDocumento(Long documento, String tipo) throws IOException, InterruptedException;
    File exportaResultadosEnquete(Long enquete, String tipo) throws IOException, InterruptedException;
}
