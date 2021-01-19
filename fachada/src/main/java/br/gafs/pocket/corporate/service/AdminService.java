/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.dto.FiltroEmpresaDTO;
import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Plano;
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
    
    BuscaPaginadaDTO<Empresa> busca(FiltroEmpresaDTO filtro);
    void cadastra(Empresa empresa);
    void atualiza(Empresa empresa);
    Empresa buscaEmpresa(String chave);
    void inativa(Long empresa);
    
    List<Plano> buscaTodos();
    void cadastra(Plano plano);
    void atualiza(Plano plano);
    Plano buscaPlano(Long id);
}
