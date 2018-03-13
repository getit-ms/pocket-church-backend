/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.MenuDTO;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.Usuario;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Gabriel
 */
public interface AcessoService extends Serializable {
    Membro login(String username, String password, TipoDispositivo tipo, String version);
    Usuario admin(String username, String password);
    Membro refreshLogin();
    Usuario refreshAdmin();
    void logout();

    List<Ministerio> buscaMinisterios();
    Preferencias buscaPreferencis();
    Preferencias salva(Preferencias preferencias);
    List<Funcionalidade> getFuncionalidadesMembro();
    List<Funcionalidade> getTodasFuncionalidadesAdmin();

    void registerPush(TipoDispositivo tipoDispositivo, String pushKey, String version);

    void alteraSenha(Membro entidade);
    
    void solicitaRedefinicaoSenha(String email);
    
    Membro redefineSenha(String jwt);

    List<Funcionalidade> buscaFuncionalidadesPublicas();

    MenuDTO buscaMenu(int versaoMajor, int versaoMinor, int versaoBugfix);
}
