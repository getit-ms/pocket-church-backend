/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.dto.MenuDTO;
import br.gafs.pocket.corporate.dto.ResumoEmpresaDTO;
import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.Colaborador;
import br.gafs.pocket.corporate.entity.Preferencias;
import br.gafs.pocket.corporate.entity.Usuario;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Gabriel
 */
public interface AcessoService extends Serializable {

    List<ResumoEmpresaDTO> inciaLogin(String username);
    Colaborador login(String username, String password, TipoDispositivo tipo, String version);
    Usuario admin(String username, String password);
    Colaborador refreshLogin();
    Usuario refreshAdmin();
    void logout();

    Preferencias buscaPreferencis();
    Preferencias salva(Preferencias preferencias);
    List<Funcionalidade> getFuncionalidadesColaborador();
    List<Funcionalidade> getTodasFuncionalidadesAdmin();

    void registerPush(TipoDispositivo tipoDispositivo, String pushKey, String version);

    void alteraSenha(Colaborador entidade);
    
    void solicitaRedefinicaoSenha(String email);
    
    Colaborador redefineSenha(String jwt);

    MenuDTO buscaMenu(int versaoMajor, int versaoMinor, int versaoBugfix);

    void trocaFoto(Arquivo arquivo);
}
