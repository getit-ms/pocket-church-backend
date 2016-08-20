/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

import java.util.ArrayList;
import java.util.List;
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
    MANTER_DADOS_INSTITUCIONAIS(Tipo.ADMIN, 1),
    MANTER_MINISTERIOS(Tipo.ADMIN, 2),
    MANTER_PERFIS(Tipo.ADMIN, 3),
    MANTER_MEMBROS(Tipo.ADMIN, true, 4),
    MANTER_BOLETINS(Tipo.ADMIN, 5),
    MANTER_ESTUDOS(Tipo.ADMIN, 6),
    MANTER_VOTACOES(Tipo.ADMIN, 7),
    CONSULTAR_PEDIDOS_ORACAO(Tipo.ADMIN, 8),
    MANTER_AGENDA(Tipo.ADMIN, 8),
    MANTER_EVENTOS(Tipo.ADMIN, 10),
    GERENCIAR_ACESSO_MEMBROS(Tipo.ADMIN, 11),
    GERENCIAR_FUNCIONALIDADES_APLICATIVO(Tipo.ADMIN, 12),
    ENVIAR_NOTIFICACOES(Tipo.ADMIN, true, 13),
    MANTER_VERSICULOS_DIARIOS(Tipo.ADMIN, 14),
    MANTER_CIFRAS(Tipo.ADMIN, 15),
    CONFIGURAR(Tipo.ADMIN, 16),
    ABERTURA_CHAMADO_SUPORTE(Tipo.ADMIN, 18),

    // Membro
    CONSULTAR_CONTATOS_IGREJA(Tipo.MEMBRO, 19),
    REALIZAR_VOTACAO(Tipo.MEMBRO, 20),
    PEDIR_ORACAO(Tipo.MEMBRO, 21),
    AGENDAR_ACONSELHAMENTO(Tipo.MEMBRO, 22),
    REALIZAR_INSCRICAO_EVENTO(Tipo.MEMBRO, 23)
    ;
    
    public final static List<Funcionalidade> FUNCIONALIDADES_APLICATIVO = new ArrayList<Funcionalidade>();
    public final static List<Funcionalidade> FUNCIONALIDADES_ADMINISTRATIVO = new ArrayList<Funcionalidade>();
    
    static {
        for (Funcionalidade func : values()){
            if (func.isAdmin()){
                FUNCIONALIDADES_ADMINISTRATIVO.add(func);
            }else{
                FUNCIONALIDADES_APLICATIVO.add(func);
            }
        }
    }
    
    private final Tipo tipo;
    private final boolean associaMinisterios;
    private final int codigo;

    private final String chave = name().toLowerCase().replace("[^a-z]", ".");

    private Funcionalidade(Tipo tipo, int numero) {
        this(tipo, false, numero);
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
