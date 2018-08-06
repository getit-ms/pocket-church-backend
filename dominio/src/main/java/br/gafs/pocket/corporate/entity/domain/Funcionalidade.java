/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum Funcionalidade {
    // Administrador
    MANTER_DADOS_INSTITUCIONAIS(Tipo.ADMIN, 1),
    MANTER_PERFIS(Tipo.ADMIN, 3),
    MANTER_COLABORADORES(Tipo.ADMIN, true, 4),
    MANTER_BOLETINS(Tipo.ADMIN, 5),
    MANTER_DOCUMENTOS(Tipo.ADMIN, 6),
    MANTER_ENQUETES(Tipo.ADMIN, 7),
    CONSULTAR_CONTATOS_COLABORADORES(Tipo.ADMIN, 8),
    MANTER_AGENDA(Tipo.ADMIN, 9),
    MANTER_EVENTOS(Tipo.ADMIN, 10),
    GERENCIAR_ACESSO_COLABORADORES(Tipo.ADMIN, 11),
    GERENCIAR_FUNCIONALIDADES_APLICATIVO(Tipo.ADMIN, 12),
    ENVIAR_NOTIFICACOES(Tipo.ADMIN, true, 13),
    MANTER_MENSAGENS_DIA(Tipo.ADMIN, 14),
    MANTER_NOTICIAS(Tipo.ADMIN, 47),
    MANTER_CLASSIFICADOS(Tipo.ADMIN, 53),
    CONFIGURAR(Tipo.ADMIN, 16),
    ABERTURA_CHAMADO_SUPORTE(Tipo.ADMIN, 18),
    CONFIGURAR_YOUTUBE(Tipo.ADMIN, 30),
    MANTER_PUBLICACOES(Tipo.ADMIN, 35),
    CONFIGURAR_GOOGLE_CALENDAR(Tipo.ADMIN, 36),
    MANTER_AUDIOS(Tipo.ADMIN, 49),
    CONFIGURAR_FLICKR(Tipo.ADMIN, 50),

    // Colaborador
    CONSULTAR_CONTATOS_EMPRESA(Tipo.COLABORADOR, 19),
    RESPONDER_ENQUETE(Tipo.COLABORADOR, false, 20),
    ENVIAR_CONTATO_COLABORADOR(Tipo.COLABORADOR, 21),
    REALIZAR_AGENDAMENTO(Tipo.COLABORADOR, 22),
    REALIZAR_INSCRICAO_EVENTO(Tipo.COLABORADOR, 23),
    ANIVERSARIANTES(Tipo.COLABORADOR, false, 48),
    GALERIA_FOTOS(Tipo.COLABORADOR, false, 52),
    LISTAR_BOLETINS(Tipo.COLABORADOR, 24),
    LISTAR_DOCUMENTOS(Tipo.COLABORADOR, 25),
    YOUTUBE(Tipo.COLABORADOR, 31),
    AGENDA(Tipo.COLABORADOR, 37),
    LISTAR_PUBLICACOES(Tipo.COLABORADOR, 38),
    NOTICIAS(Tipo.COLABORADOR, false, 44),
    CLASSIFICADOS(Tipo.COLABORADOR, false, 54),
    AUDIOS(Tipo.COLABORADOR, false, 51, 7, 1, 0),


    INSTITUCIONAL(Tipo.FIXA, 43),
    INICIO_APLICATIVO(Tipo.FIXA, 39),
    NOTIFICACOES(Tipo.FIXA, 40),
    PREFERENCIAS(Tipo.FIXA, 42),

    ;
    
    public final static List<Funcionalidade> FUNCIONALIDADES_APLICATIVO = new ArrayList<Funcionalidade>();
    public final static List<Funcionalidade> FUNCIONALIDADES_ADMINISTRATIVO = new ArrayList<Funcionalidade>();
    public final static List<Funcionalidade> FUNCIONALIDADES_FIXAS = new ArrayList<Funcionalidade>();

    static {
        for (Funcionalidade func : values()){
            if (func.isAdmin()){
                FUNCIONALIDADES_ADMINISTRATIVO.add(func);
            }else if (func.isColaborador()){
                FUNCIONALIDADES_APLICATIVO.add(func);
            }else{
                FUNCIONALIDADES_FIXAS.add(func);
            }
        }
    }
    
    private final Tipo tipo;
    private final boolean associaMinisterios;
    private final int codigo;
    private int versaoMajor = 0;
    private int versaoMinor = 0;
    private int versaoBugfix = 0;

    private final String chave = name().toLowerCase().replace("[^a-z]", ".");

    private Funcionalidade(Tipo tipo, int numero) {
        this(tipo, false, numero);
    }

    public boolean isAdmin() {
        return Tipo.ADMIN.equals(tipo);
    }

    public boolean isColaborador() {
        return Tipo.COLABORADOR.equals(tipo);
    }
    
    enum Tipo {
        ADMIN,
        COLABORADOR,
        FIXA
    }
    
}
