/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.FiltroIgrejaDTO;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Plano;
import br.gafs.dao.BuscaPaginadaDTO;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Gabriel
 */
public interface AdminService extends Serializable {
    Arquivo upload(String fileName, byte[] fileData);
    Arquivo buscaArquivo(Long arquivo);
    
    BuscaPaginadaDTO<Igreja> busca(FiltroIgrejaDTO filtro);
    void cadastra(Igreja igreja);
    void atualiza(Igreja igreja);
    Igreja buscaIgreja(String chave);
    void inativa(Long igreja);
    
    List<Plano> buscaTodos();
    void cadastra(Plano plano);
    void atualiza(Plano plano);
    Plano buscaPlano(Long id);
}
