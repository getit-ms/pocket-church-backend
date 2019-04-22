/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

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
    MANTER_MINISTERIOS(Tipo.ADMIN, 2),
    MANTER_PERFIS(Tipo.ADMIN, 3),
    MANTER_MEMBROS(Tipo.ADMIN, true, 4),
    MANTER_BOLETINS(Tipo.ADMIN, 5),
    MANTER_ESTUDOS(Tipo.ADMIN, 6),
    MANTER_VOTACOES(Tipo.ADMIN, 7),
    CONSULTAR_PEDIDOS_ORACAO(Tipo.ADMIN, 8),
    MANTER_AGENDA(Tipo.ADMIN, 9),
    MANTER_EVENTOS(Tipo.ADMIN, 10),
    MANTER_EBD(Tipo.ADMIN, 28),
    GERENCIAR_ACESSO_MEMBROS(Tipo.ADMIN, 11),
    GERENCIAR_FUNCIONALIDADES_APLICATIVO(Tipo.ADMIN, 12),
    ENVIAR_NOTIFICACOES(Tipo.ADMIN, true, 13),
    MANTER_VERSICULOS_DIARIOS(Tipo.ADMIN, 14),
    MANTER_CIFRAS(Tipo.ADMIN, 15),
    MANTER_CANTICOS(Tipo.ADMIN, 45),
    MANTER_NOTICIAS(Tipo.ADMIN, 47),
    CONFIGURAR(Tipo.ADMIN, 16),
    ABERTURA_CHAMADO_SUPORTE(Tipo.ADMIN, 18),
    CONFIGURAR_YOUTUBE(Tipo.ADMIN, 30),
    MANTER_PLANOS_LEITURA_BIBLICA(Tipo.ADMIN, 33),
    MANTER_PUBLICACOES(Tipo.ADMIN, 35),
    CONFIGURAR_GOOGLE_CALENDAR(Tipo.ADMIN, 36),
    MANTER_AUDIOS(Tipo.ADMIN, 49),
    CONFIGURAR_FLICKR(Tipo.ADMIN, 50),
    CONFIGURAR_VIDEOS_FACEBOOK(Tipo.ADMIN, 53),

    // Membro
    CONSULTAR_CONTATOS_IGREJA(Tipo.MEMBRO, 19),
    REALIZAR_VOTACAO(Tipo.MEMBRO, false, 20, 5, 0, 0),
    PEDIR_ORACAO(Tipo.MEMBRO, 21),
    AGENDAR_ACONSELHAMENTO(Tipo.MEMBRO, 22),
    REALIZAR_INSCRICAO_EVENTO(Tipo.MEMBRO, 23),
    REALIZAR_INSCRICAO_EBD(Tipo.MEMBRO, 29),
    CONSULTAR_PLANOS_LEITURA_BIBLICA(Tipo.MEMBRO, 34),
    ANIVERSARIANTES(Tipo.MEMBRO, false, 48, 5, 0, 0),
    GALERIA_FOTOS(Tipo.MEMBRO, false, 52, 6, 0, 11),

    // Pública
    LISTAR_BOLETINS(Tipo.PUBLICA, 24),
    LISTAR_ESTUDOS(Tipo.PUBLICA, 25),
    CONSULTAR_HINARIO(Tipo.PUBLICA, 26),
    CONSULTAR_CIFRAS(Tipo.PUBLICA, 27),
    YOUTUBE(Tipo.PUBLICA, 31),
    BIBLIA(Tipo.PUBLICA, 32),
    AGENDA(Tipo.PUBLICA, 37),
    LISTAR_PUBLICACOES(Tipo.PUBLICA, 38),
    NOTICIAS(Tipo.PUBLICA, false, 44, 4, 2, 0),
    CONSULTAR_CANTICOS(Tipo.PUBLICA, false, 46, 4, 2, 0),
    AUDIOS(Tipo.PUBLICA, false, 51, 6, 0, 11),


    INSTITUCIONAL(Tipo.FIXA, 43),
    INICIO_APLICATIVO(Tipo.FIXA, 39),
    NOTIFICACOES(Tipo.FIXA, 40),
    CHAMADOS(Tipo.FIXA, 41),
    PREFERENCIAS(Tipo.FIXA, 42),

    ;
    
    public final static List<Funcionalidade> FUNCIONALIDADES_APLICATIVO = new ArrayList<Funcionalidade>();
    public final static List<Funcionalidade> FUNCIONALIDADES_PUBLICAS = new ArrayList<Funcionalidade>();
    public final static List<Funcionalidade> FUNCIONALIDADES_ADMINISTRATIVO = new ArrayList<Funcionalidade>();
    public final static List<Funcionalidade> FUNCIONALIDADES_FIXAS = new ArrayList<Funcionalidade>();

    static {
        for (Funcionalidade func : values()){
            if (func.isAdmin()){
                FUNCIONALIDADES_ADMINISTRATIVO.add(func);
            }else if (func.isMembro()){
                FUNCIONALIDADES_APLICATIVO.add(func);
            }else if (func.isPublica()){
                FUNCIONALIDADES_PUBLICAS.add(func);
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

    public boolean isMembro() {
        return Tipo.MEMBRO.equals(tipo);
    }
    
    public boolean isPublica(){
        return Tipo.PUBLICA.equals(tipo);
    }

    public static Funcionalidade getByCodigo(Integer codigo) {
        if (codigo == null) {
            return null;
        }

        for (Funcionalidade func : values()) {
            if (codigo.equals(func.getCodigo())) {
                return func;
            }
        }

        return null;
    }

    enum Tipo {
        ADMIN,
        MEMBRO,
        PUBLICA,
        FIXA
    }
    
}
