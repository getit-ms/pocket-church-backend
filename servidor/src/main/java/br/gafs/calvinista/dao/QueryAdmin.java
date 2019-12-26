/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.entity.domain.*;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author Gabriel
 */
public enum QueryAdmin {
    REFERENCIAS_INSCRICOES_PENDENTES("InscricaoEvento.findReferenciasByStatusAndIgreja", "igreja"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusInscricaoEvento.PENDENTE);
        }
    },
    INSCRICOES_POR_REFERENCIA("InscricaoEvento.findByReferencia", "referencia"),
    ULTIMA_INSCRICAO("InscricaoEvento.findMaxDataByEvento", "evento", "igreja"),
    VERSICULOS_POR_STATUS("VersiculoDiario.findByIgrejaAndStatus", "chaveIgreja", "status"),
    REMOVER_VOTOS("Voto.removerPorVotacao", "chaveIgreja", "idVotacao"),
    REMOVER_RESPOSTAS_OPCAO("RespostaOpcao.removerPorVotacao", "chaveIgreja", "idVotacao"),
    REMOVER_RESPOSTAS_QUESTAO("RespostaQuestao.removerPorVotacao", "chaveIgreja", "idVotacao"),
    REMOVER_RESPOSTAS_VOTACAO("RespostaVotacao.removerPorVotacao", "chaveIgreja", "idVotacao"),
    ATUALIZA_VAGAS_EVENTO("Evento.atualizaVagasEvento", "evento", "igreja", "decremento"),
    MINISTERIOS_POR_ACESSO("Ministerio.findByAcesso", "chaveIgreja", "idMembro"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("chaveIgreja", args[0]).
                    set("idMembro", args[1]).set("status", StatusMinisterio.ATIVO);
        }
        
    }, 
    REGISTRA_USO_ARQUIVO("Arquivo.registraUso", "arquivo", "igreja"),
    REGISTRA_DESUSO_ARQUIVO("Arquivo.registraDesuso", "arquivo", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("timeout", new Date(System.currentTimeMillis() + DateUtil.MILESIMOS_POR_DIA));
        }
        
    },
    MINISTERIOS_ATIVOS("Ministerio.findByStatusAndIgreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("status", StatusMinisterio.ATIVO).
                    set("idIgreja", args[0]);
        }
        
    }, 
    PERFIS("Perfil.findByIgreja", "idIgreja"), 
    AGENDAMENTOS_ATENDIMENTO("AgendamentoAtendimento.findByStatusCalendarioPeriodo"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("idCalendario", args[1]).
                    set("chaveIgreja", args[0]).
                    set("dataInicio", args[2]).
                    set("dataTermino", args[3]).
                    set("status", Arrays.asList(
                            StatusAgendamentoAtendimento.NAO_CONFIRMADO,
                            StatusAgendamentoAtendimento.CONFIRMADO
                    ));
        }
        
    }, 
    CALENDARIOS("CalendarioAtendimento.findByIgreja", "idIgreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("idIgreja", args[0]).set("status", StatusCalendario.ATIVO);
        }
        
    }, 
    AGENDAMENTO_EM_CHOQUE("AgendamentoAtendimento.findAgendamentoEmChoque"){
        
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("status", Arrays.asList(
                        StatusAgendamentoAtendimento.CONFIRMADO,
                        StatusAgendamentoAtendimento.NAO_CONFIRMADO)).
                    set("idCalendario", args[0]).
                    set("dataInicio", args[1]).
                    set("dataTermino", args[2]);
        }
        
    }, 
    SORTEIA_VERSICULO("VersiculoDiario.sorteiaByIgreja", "idIgreja"){
        private Random rand = new Random(System.currentTimeMillis());
        
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("status", StatusVersiculoDiario.HABILITADO).
                    set("rand", rand.nextInt(1000) + 1);
        }
        
    }, 
    IGREJAS_ATIVAS("Igreja.findAtivas"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("status", Arrays.asList(StatusIgreja.ATIVO));
        }
        
    }, 
    MINISTERIO_ATIVO_POR_IGREJA("Ministerio.findByStatusAndIgrejaAndId"){
        
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("status", StatusMinisterio.ATIVO).
                    set("idIgreja", args[0]).set("idMinisterio", args[1]);
        }
        
    }, 
    ARQUIVOS_VENCIDOS("Arquivo.findVencidos"), 
    PASTORES_ATIVOS("Membro.findPastor"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("chaveIgreja", args[0]).
                    set("statusCalendario", StatusCalendario.ATIVO).
                    set("status", Arrays.asList(StatusMembro.CONTATO, StatusMembro.MEMBRO));
        }
        
    }, 
    HORARIOS_POR_PERIODO("HorarioAtendimento.findByCalendarioAndPeriodo", "idCalendario", "dataInicio", "dataFim"), 
    RESULTADOS_OPCAO("RespostaQuestao.findCountByOpcao", "opcao"), 
    BRANCOS_QUESTAO("RespostaQuestao.findCountBrancos", "questao"), 
    NULOS_QUESTAO("RespostaQuestao.findCountNulos", "questao"), 
    MEMBRO_POR_EMAIL_IGREJA("Membro.findByEmailIgreja", "email", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("status", Arrays.asList(StatusMembro.CONTATO, StatusMembro.MEMBRO));
        }
        
    },
    BUSCA_QUANTIDADE_INSCRICOES("InscricaoEvento.quantidadeInscricoesEvento", "idEvento"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", Arrays.asList(
                            StatusInscricaoEvento.CONFIRMADA,
                            StatusInscricaoEvento.PENDENTE));
        }
        
    }, 
    PREFERENCIAS_POR_MEMBRO("Preferencias.findByMembro", "membro", "igreja"), 
    CALENDARIO_ATIVO_POR_PASTOR("CalendarioAtendimeto.findByPastorAndIgrejaAndStatus", "igreja", "pastor"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusCalendario.ATIVO);
        }
        
    }, 
    MENOR_ENVIO_VERSICULOS("VersiculoDiario.findMenorEnvioByIgrejaAndStatus", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusVersiculoDiario.HABILITADO);
        }
        
    }, 
    DESABILITA_DISPOSITIVO_BY_PUSHKEY("Dispositivo.desabilitaByPushkey", "pushkey"),
    RELEASE_NOTES("ReleaseNotes.findByTipo", "tipo"){
        @Override
        protected int extractResultLimit(Object... args) {
            return 10;
        }
    },
    UPDATE_BOLETINS_NAO_DIVULGADOS("Boletim.updateNaoDivulgadosByIgreja", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("data", DateUtil.getDataAtual())
                    .set("tipo", TipoBoletim.BOLETIM);
        }

    },
    UPDATE_PUBLICACOES_NAO_DIVULGADOS("Boletim.updateNaoDivulgadosByIgreja", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("data", DateUtil.getDataAtual())
                    .set("tipo", TipoBoletim.PUBLICACAO);
        }

    },
    UPDATE_ESTUDOS_NAO_DIVULGADOS("Estudo.updateNaoDivulgadosByIgreja", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    UPDATE_NOTICIAS_NAO_DIVULGADAS("Noticia.updateNaoDivulgadosByIgreja", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    IGREJAS_ATIVAS_COM_DEVOCIONARIO_A_DIVULGAR("DiaDevocionario.findIgrejaByStatusAndDataPublicacao"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("statusIgreja", StatusIgreja.ATIVO)
                    .set("statusDiaDevocionario", StatusDiaDevocionario.PUBLICADO)
                    .set("data", DateUtil.retirarHora(DateUtil.getDataAtual()));
        }
    },
    IGREJAS_ATIVAS_COM_BOLETINS_A_DIVULGAR("Boletim.findIgrejaByStatusAndDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusIgreja", StatusIgreja.ATIVO).
                    set("statusBoletim", StatusBoletim.PUBLICADO).
                    set("tipo", TipoBoletim.BOLETIM).
                    set("data", DateUtil.getDataAtual());
        }
        
    },
    IGREJAS_ATIVAS_COM_PUBLICACOES_A_DIVULGAR("Boletim.findIgrejaByStatusAndDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusIgreja", StatusIgreja.ATIVO).
                    set("statusBoletim", StatusBoletim.PUBLICADO).
                    set("tipo", TipoBoletim.PUBLICACAO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    IGREJAS_ATIVAS_COM_ESTUDOS_A_DIVULGAR("Estudo.findIgrejaNaoDivultadosByDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusIgreja", StatusIgreja.ATIVO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    IGREJAS_ATIVAS_COM_NOTICIAS_A_DIVULGAR("Noticia.findIgrejaNaoDivultadosByDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusIgreja", StatusIgreja.ATIVO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    ANIVERSARIANTES("AniversarioMembro.findAniversariantes", "igreja"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusMembro.EXCLUIDO);
        }
    },
    PROXIMOS_ANIVERSARIANTES("AniversarioMembro.findProximosAniversariantes", "igreja", "inicio", "fim"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusMembro.EXCLUIDO);
        }
    },
    BOLETINS_PROCESSANDO("Boletim.findByStatus"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusBoletim.PROCESSANDO);
        }

        @Override
        protected int extractResultLimit(Object... args) {
            return 5;
        }
        
    }, 
    ESTUDOS_PROCESSANDO("Estudo.findPDFByStatus"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusEstudo.PROCESSANDO);
        }

        @Override
        protected int extractResultLimit(Object... args) {
            return 5;
        }

    },
    UPDATE_STATUS_BOLETIM("Boletim.updateStatus", "igreja", "boletim", "status"),
    UPDATE_STATUS_ESTUDO("Estudo.updateStatus", "igreja", "estudo", "status"),
    COUNT_LEITURA_SELECIONADA("MarcacaoLeituraBiblica.countLeituraSelecionada", "chaveIgreja", "idMembro", "ultimaAlteracao"),
    LEITURA_SELECIONADA("MarcacaoLeituraBiblica.findLeituraSelecionada", COUNT_LEITURA_SELECIONADA, "chaveIgreja", "idMembro", "ultimaAlteracao"){

        @Override
        protected int extractResultLimit(Object... args) {
            return (Integer) args[3];
        }
        
    }, 
    OPCAO_PLANO_LEITURA_SELECIONADA("OpcaoLeituraBiblica.findOpcaoSelecionad", "chaveIgreja", "idMembro"), 
    MARCACAO_LEITURA_DIA("MarcacaoLeituraBiblica.findByMembroAndDia", "chaveIgreja", "idMembro", "idDia"), 
    DIA_PLANO("DiaLeituraBiblica.findByPlanoAndData", "chaveIgreja", "idPlano", "data"), 
    REMOVE_OPCAO_PLANO("OpcaoLeituraBiblica.removeByPlano", "chaveIgreja", "idPlano"),
    REMOVE_MARCACAO_PLANO("MarcacaoLeituraBiblica.removeByPlano", "chaveIgreja", "idPlano"),
    INSCRICOES_EVENTOS_ATIVOS("InscricaoEvento.findAtivosByIgreja", "tipo", "chaveIgreja"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("statusInscricao", StatusInscricaoEvento.CONFIRMADA).set("statusEvento", StatusEvento.ATIVO);
        }
    },
    CATEGORIA_ESTUDO_POR_IGREJA_NOME("CategoriaEstudo.findByIgrejaAndNome", "igreja", "nome"),
    CATEGORIA_ESTUDO("CategoriaEstudo.findByIgreja", "igreja"),
    CATEGORIA_AUDIO("CategoriaAudio.findByIgreja", "igreja"),
    CATEGORIA_USADAS_ESTUDO("CategoriaEstudo.findUsadasByIgreja", "igreja"),
    CATEGORIA_USADAS_AUDIO("CategoriaAudio.findUsadasByIgreja", "igreja"),
    MENUS_IGREJA_FUNCIONALIDADES("Menu.findByIgrejaAndFuncionalidades", "igreja", "funcionalidades"),
    BUSCA_NSCRICOES_EMAIL("InscricaoEvento.findInscricoesMembro", "tipo", "igreja", "email"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("statusEvento", StatusEvento.ATIVO)
                    .set("statusInscricao", Arrays.asList(
                            StatusInscricaoEvento.PENDENTE,
                            StatusInscricaoEvento.CONFIRMADA
                    ));
        }
    },
    NOTICIA_A_DIVULGAR_POR_IGREJA("Noticia.findUltimaADivulgar", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    ESTUDO_A_DIVULGAR_POR_IGREJA("Estudo.findUltimoADivulgar", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    PUBLICACAO_A_DIVULGAR_POR_IGREJA("Boletim.findUltimoADivulgar", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("tipo", TipoBoletim.PUBLICACAO).
                    set("statusBoletim", StatusBoletim.PUBLICADO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    DEVOCIONARIO_POR_IGREJA_DATA("DiaDevocionario.findByIgrejaAndData", "igreja", "data") {
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("data", DateUtil.retirarHora((Date) args[1]));
        }
    },
    BOLETIM_A_DIVULGAR_POR_IGREJA("Boletim.findUltimoADivulgar", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("tipo", TipoBoletim.BOLETIM).
                    set("statusBoletim", StatusBoletim.PUBLICADO).
                    set("data", DateUtil.getDataAtual());
        }
    },
    REMOVE_EVENTO_CALENDARIO_POR_IGREJA("EventoCalendario.removeDesatualizadosPorIgreja", "igreja", "limite"),
    COUNT_EVENTOS_CALENDARIO_IGREJA("EventoCalendario.countByIgreja", "igreja"),
    EVENTOS_CALENDARIO_IGREJA("EventoCalendario.findByIgreja", COUNT_EVENTOS_CALENDARIO_IGREJA, "igreja"){

        @Override
        protected int extractResultLimit(Object... args) {
            return (int) args[1];
        }
    },
    QUANTIDADE_DISPOSITIVOS_BY_IGREJA("EstatisticaDispositivo.quantidadeDispositivosIgreja", "igreja") {
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("tipos", Arrays.asList(
                            TipoDispositivo.ANDROID,
                            TipoDispositivo.IPB,
                            TipoDispositivo.IPHONE,
                            TipoDispositivo.ANDROID_FIREBASE,
                            TipoDispositivo.IPHONE_FIREBASE
                    ))
                    .set("limite", DateUtil.decrementaMeses(new Date(), 1));
        }
    },
    ESTATISTICAS_DISPOSITIVOS_BY_IGREJA("EstatisticaDispositivo.findByIgreja", "igreja"),
    ESTATISTICAS_ACESSO_BY_IGREJA_AND_FUNCIONALIDADE("EstatisticaAcesso.findByIgrejaAndFuncionalidade", "igreja", "funcionalidade"),
    ESTATISTICAS_DISPOSITIVOS_ONLINE("EstatisticaDispositivo.findOnLine") {
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("tipos", Arrays.asList(
                            TipoDispositivo.ANDROID,
                            TipoDispositivo.IPB,
                            TipoDispositivo.IPHONE,
                            TipoDispositivo.ANDROID_FIREBASE,
                            TipoDispositivo.IPHONE_FIREBASE
                    ))
                    .set("limite", DateUtil.getDataAtualPrimeiraHora())
                    .set("statusIgreja", StatusIgreja.ATIVO);
        }
    },
    ESTATISTICAS_ACESSO_ONLINE("EstatisticaAcesso.findOnLine") {
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("inicio", DateUtil.getDataAtualPrimeiraHora())
                    .set("termino", DateUtil.getDataAtualUltimaHora())
                    .set("sucesso", StatusRegistroAcesso.SUCESSO)
                    .set("falha", StatusRegistroAcesso.FALHA)
                    .set("statusIgreja", StatusIgreja.ATIVO);
        }
    },
    REMOVE_ESTATISTICAS_DISPOSITIVOS_IGREJAS_ANTIGAS("EstatisticaDispositivo.removeAntigas"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("limite", DateUtil.decrementaMeses(new Date(), 1));
        }
    },
    REMOVE_ESTATISTICAS_ACESSO_IGREJAS_ANTIGAS("EstatisticaAcesso.removeAntigas"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("limite", DateUtil.decrementaMeses(new Date(), 1));
        }
    },
    REMOVE_REGISTROS_ACESSO_IGREJAS_ANTIGAS("RegistroAcesso.removeAntigas"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("limite", DateUtil.decrementaDia(new Date(), 7));
        }
    };

    private final String query;
    private final String[] parameters;
    private final QueryAdmin countQuery;

    private QueryAdmin(String query, String... parameters) {
        this(query, (QueryAdmin) null, parameters);
    }
    
    private QueryAdmin(String query, QueryAdmin countQuery, String... parameters) {
        this.query = query;
        this.parameters = parameters;
        this.countQuery = countQuery;
    }
    
   public Queries.NamedQuery create(Object... args){
       return QueryUtil.create(Queries.NamedQuery.class, 
               query, extractArguments(args), extractResultLimit(args));
   }
        
   public Queries.SingleNamedQuery createSingle(Object... args){
       return QueryUtil.create(Queries.SingleNamedQuery.class, 
               query, extractArguments(args));
   }
   
   public Queries.PaginatedNamedQuery createPaginada(int pagina, Object... args){
       return QueryUtil.create(Queries.PaginatedNamedQuery.class, 
               query, extractArguments(args), extractResultLimit(args), 
               pagina, getCountQuery().createSingle(args));
   }
   
   protected int extractResultLimit(Object... args){
       return -1;
   }
   
   protected QueryParameters extractArguments(Object... args){
       QueryParameters arguments = new QueryParameters();
       for (int i=0;i<parameters.length && i<args.length;i++){
           arguments.put(parameters[i], args[i]);
       }
       return arguments;
   }

    private QueryAdmin getCountQuery() {
        return countQuery;
    }
}
