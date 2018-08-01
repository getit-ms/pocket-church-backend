/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;

/**
 *
 * @author Gabriel
 */
public interface ArquivoService {
    Arquivo buscaArquivo(Long arquivo);
    Arquivo upload(String nome, byte[] data);
    Arquivo cadastra(Empresa empresa, String nome, byte[] data);
    void registraUso(Long idArquivo);
    void registraDesuso(Long idArquivo);
    void registraUso(String empresa, Long idArquivo);
    void registraDesuso(String empresa, Long idArquivo);
}
