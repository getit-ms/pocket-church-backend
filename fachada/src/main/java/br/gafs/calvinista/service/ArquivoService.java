/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.entity.Arquivo;

/**
 *
 * @author Gabriel
 */
public interface ArquivoService {
    Arquivo buscaArquivo(Long arquivo);
    Arquivo upload(String nome, byte[] data);
    void registraUso(Long idArquivo);
    void registraDesuso(Long idArquivo);
}
