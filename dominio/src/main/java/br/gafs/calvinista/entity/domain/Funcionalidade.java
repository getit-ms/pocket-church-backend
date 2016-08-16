/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@RequiredArgsConstructor
public enum Funcionalidade {
    // Administrador
    MANTER_DADOS_INSTITUCIONAIS(Tipo.ADMIN),
    MANTER_MINISTERIOS(Tipo.ADMIN),
    MANTER_PERFIS(Tipo.ADMIN),
    MANTER_MEMBROS(Tipo.ADMIN, true),
    MANTER_BOLETINS(Tipo.ADMIN),
    MANTER_ESTUDOS(Tipo.ADMIN),
    MANTER_VOTACOES(Tipo.ADMIN),
    CONSULTAR_PEDIDOS_ORACAO(Tipo.ADMIN),
    MANTER_AGENDA(Tipo.ADMIN),
    MANTER_EVENTOS(Tipo.ADMIN),
    GERENCIAR_ACESSO_MEMBROS(Tipo.ADMIN),
    GERENCIAR_FUNCIONALIDADES_APLICATIVO(Tipo.ADMIN),
    ENVIAR_NOTIFICACOES(Tipo.ADMIN, true),
    MANTER_VERSICULOS_DIARIOS(Tipo.ADMIN),
    MANTER_CIFRAS(Tipo.ADMIN),
    
    // Membro
    CONSULTAR_CONTATOS_IGREJA(Tipo.MEMBRO),
    REALIZAR_VOTACAO(Tipo.MEMBRO),
    PEDIR_ORACAO(Tipo.MEMBRO),
    AGENDAR_ACONSELHAMENTO(Tipo.MEMBRO),
    REALIZAR_INSCRICAO_EVENTO(Tipo.MEMBRO)
    ;
    
    private final Tipo tipo;
    
    private final boolean associaMinisterios;
    private final String chave = name().toLowerCase().replace("[^a-z]", ".");

    private Funcionalidade(Tipo tipo) {
        this(tipo, false);
    }

    public boolean isAdmin() {
        return Tipo.ADMIN.equals(tipo);
    }

    public boolean isMembro() {
        return Tipo.MEMBRO.equals(tipo);
    }
    
    enum Tipo {
        ADMIN,
        MEMBRO
    }
    
}
