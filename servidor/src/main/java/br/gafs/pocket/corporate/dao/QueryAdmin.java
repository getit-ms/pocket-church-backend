/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.pocket.corporate.entity.domain.*;
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
    REFERENCIAS_INSCRICOES_PENDENTES("InscricaoEvento.findReferenciasByStatusAndEmpresa", "empresa"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusInscricaoEvento.PENDENTE);
        }
    },
    INSCRICOES_POR_REFERENCIA("InscricaoEvento.findByReferencia", "referencia"),
    MENSAGEM_DIAS_POR_STATUS("MensagemDia.findByEmpresaAndStatus", "chaveEmpresa", "status"),
    REMOVER_VOTOS("RespostaEnqueteColaborador.removerPorEnquete", "chaveEmpresa", "idEnquete"),
    REMOVER_RESPOSTAS_OPCAO("RespostaOpcao.removerPorEnquete", "chaveEmpresa", "idEnquete"),
    REMOVER_RESPOSTAS_QUESTAO("RespostaQuestao.removerPorEnquete", "chaveEmpresa", "idEnquete"),
    REMOVER_RESPOSTAS_ENQUETE("RespostaEnquete.removerPorEnquete", "chaveEmpresa", "idEnquete"),
    REGISTRA_USO_ARQUIVO("Arquivo.registraUso", "arquivo", "empresa"),
    REGISTRA_DESUSO_ARQUIVO("Arquivo.registraDesuso", "arquivo", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("timeout", new Date(System.currentTimeMillis() + DateUtil.MILESIMOS_POR_DIA));
        }
        
    },
    PERFIS("Perfil.findByEmpresa", "idEmpresa"),
    AGENDAMENTOS_ATENDIMENTO("AgendamentoAtendimento.findByStatusCalendarioPeriodo"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("idCalendario", args[1]).
                    set("chaveEmpresa", args[0]).
                    set("dataInicio", args[2]).
                    set("dataTermino", args[3]).
                    set("status", Arrays.asList(
                            StatusAgendamentoAtendimento.NAO_CONFIRMADO,
                            StatusAgendamentoAtendimento.CONFIRMADO
                    ));
        }
        
    }, 
    CALENDARIOS("CalendarioAtendimento.findByEmpresa", "idEmpresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("idEmpresa", args[0]).set("status", StatusCalendario.ATIVO);
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
    SORTEIA_MENSAGEM_DIA("MensagemDia.sorteiaByEmpresa", "idEmpresa"){
        private Random rand = new Random(System.currentTimeMillis());
        
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("status", StatusMensagemDia.HABILITADO).
                    set("rand", rand.nextInt(1000) + 1);
        }
        
    }, 
    EMPRESAS_ATIVAS("Empresa.findAtivas"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("status", Arrays.asList(StatusEmpresa.ATIVO));
        }
        
    }, 
    ARQUIVOS_VENCIDOS("Arquivo.findVencidos"),
    GERENTES_ATIVOS("Colaborador.findGerente"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return new QueryParameters("chaveEmpresa", args[0]).
                    set("statusCalendario", StatusCalendario.ATIVO).
                    set("status", Arrays.asList(StatusColaborador.CONTATO, StatusColaborador.COLABORADOR));
        }
        
    }, 
    HORARIOS_POR_PERIODO("HorarioAtendimento.findByCalendarioAndPeriodo", "idCalendario", "dataInicio", "dataFim"), 
    RESULTADOS_OPCAO("RespostaQuestao.findCountByOpcao", "opcao"), 
    BRANCOS_QUESTAO("RespostaQuestao.findCountBrancos", "questao"), 
    NULOS_QUESTAO("RespostaQuestao.findCountNulos", "questao"), 
    COLABORADOR_POR_EMAIL_EMPRESA("Colaborador.findByEmailEmpresa", "email", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("status", Arrays.asList(StatusColaborador.CONTATO, StatusColaborador.COLABORADOR));
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
    PREFERENCIAS_POR_COLABORADOR("Preferencias.findByColaborador", "colaborador", "empresa"),
    CALENDARIO_ATIVO_POR_GERENTE("CalendarioAtendimeto.findByGerenteAndEmpresaAndStatus", "empresa", "gerente"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusCalendario.ATIVO);
        }
        
    }, 
    MENOR_ENVIO_MENSAGEM_DIAS("MensagemDia.findMenorEnvioByEmpresaAndStatus", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusMensagemDia.HABILITADO);
        }
        
    }, 
    RELEASE_NOTES("ReleaseNotes.findByTipo", "tipo"){
        @Override
        protected int extractResultLimit(Object... args) {
            return 10;
        }
    }, 
    UPDATE_BOLETINS_NAO_DIVULGADOS("BoletimInformativo.updateNaoDivulgadosByEmpresa", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("data", DateUtil.getDataAtual())
                    .set("tipo", TipoBoletimInformativo.BOLETIM);
        }
        
    },
    UPDATE_PUBLICACOES_NAO_DIVULGADOS("BoletimInformativo.updateNaoDivulgadosByEmpresa", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("data", DateUtil.getDataAtual())
                    .set("tipo", TipoBoletimInformativo.PUBLICACAO);
        }

    },
    UPDATE_DOCUMENTOS_NAO_DIVULGADOS("Documento.updateNaoDivulgadosByEmpresa", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    UPDATE_NOTICIAS_NAO_DIVULGADAS("Noticia.updateNaoDivulgadosByEmpresa", "empresa", "tipoNoticia"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    EMPRESAS_ATIVAS_COM_BOLETINS_A_DIVULGAR("BoletimInformativo.findEmpresaByStatusAndDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusEmpresa", StatusEmpresa.ATIVO).
                    set("statusBoletim", StatusBoletimInformativo.PUBLICADO).
                    set("tipo", TipoBoletimInformativo.BOLETIM).
                    set("data", DateUtil.getDataAtual());
        }
        
    },
    EMPRESAS_ATIVAS_COM_PUBLICACOES_A_DIVULGAR("BoletimInformativo.findEmpresaByStatusAndDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusEmpresa", StatusEmpresa.ATIVO).
                    set("statusBoletim", StatusBoletimInformativo.PUBLICADO).
                    set("tipo", TipoBoletimInformativo.PUBLICACAO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    EMPRESAS_ATIVAS_COM_DOCUMENTOS_A_DIVULGAR("Documento.findEmpresaNaoDivultadosByDataPublicacao"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusEmpresa", StatusEmpresa.ATIVO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    EMPRESAS_ATIVAS_COM_NOTICIAS_A_DIVULGAR("Noticia.findEmpresaNaoDivultadosByDataPublicacao", "tipoNoticia"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("statusEmpresa", StatusEmpresa.ATIVO).
                    set("data", DateUtil.getDataAtual());
        }

    },
    ANIVERSARIANTES("AniversarioColaborador.findAniversariantes", "empresa"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusColaborador.EXCLUIDO);
        }
    },
    PROXIMOS_ANIVERSARIANTES("AniversarioColaborador.findProximosAniversariantes", "empresa", "inicio", "fim"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusColaborador.EXCLUIDO);
        }
    },
    BOLETINS_PROCESSANDO("BoletimInformativo.findByStatus"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusBoletimInformativo.PROCESSANDO);
        }

        @Override
        protected int extractResultLimit(Object... args) {
            return 5;
        }
        
    }, 
    DOCUMENTOS_PROCESSANDO("Documento.findPDFByStatus"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("status", StatusDocumento.PROCESSANDO);
        }

        @Override
        protected int extractResultLimit(Object... args) {
            return 5;
        }

    },
    UPDATE_STATUS_BOLETIM("BoletimInformativo.updateStatus", "empresa", "boletim", "status"),
    UPDATE_STATUS_DOCUMENTO("Documento.updateStatus", "empresa", "documento", "status"),
    COUNT_LEITURA_SELECIONADA("MarcacaoLeituraBiblica.countLeituraSelecionada", "chaveEmpresa", "idColaborador", "ultimaAlteracao"),
    INSCRICOES_EVENTOS_ATIVOS("InscricaoEvento.findAtivosByEmpresa", "tipo", "chaveEmpresa"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("statusInscricao", StatusInscricaoEvento.CONFIRMADA).set("statusEvento", StatusEvento.ATIVO);
        }
    },
    CATEGORIA_DOCUMENTO_POR_EMPRESA_NOME("CategoriaDocumento.findByEmpresaAndNome", "empresa", "nome"),
    CATEGORIA_AUDIO_POR_EMPRESA_NOME("CategoriaAudio.findByEmpresaAndNome", "empresa", "nome"),
    LOTACAO_COLABORADOR_POR_EMPRESA_NOME("LotacaoColaborador.findByEmpresaAndNome", "empresa", "nome"),
    LOTACAO_COLABORADOR("LotacaoColaborador.findByEmpresa", "empresa"),
    CATEGORIA_DOCUMENTO("CategoriaDocumento.findByEmpresa", "empresa"),
    CATEGORIA_AUDIO("CategoriaAudio.findByEmpresa", "empresa"),
    CATEGORIA_USADAS_DOCUMENTO("CategoriaDocumento.findUsadasByEmpresa", "empresa"),
    CATEGORIA_USADAS_AUDIO("CategoriaAudio.findUsadasByEmpresa", "empresa"),
    MENUS_EMPRESA_FUNCIONALIDADES("Menu.findByEmpresaAndFuncionalidades", "empresa", "funcionalidades"),
    NOTICIA_A_DIVULGAR_POR_EMPRESA("Noticia.findUltimaADivulgar", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("tipoNoticia", TipoNoticia.NOTICIA).
                    set("data", DateUtil.getDataAtual());
        }

    },
    CLASSIFICADOS_A_DIVULGAR_POR_EMPRESA("Noticia.findUltimaADivulgar", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("tipoNoticia", TipoNoticia.CLASSIFICADOS).
                    set("data", DateUtil.getDataAtual());
        }

    },
    BOLETIM_A_DIVULGAR_POR_EMPRESA("BoletimInformativo.findUltimoADivulgar", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("tipo", TipoBoletimInformativo.BOLETIM).
                    set("statusBoletim", StatusBoletimInformativo.PUBLICADO).
                    set("data", DateUtil.getDataAtual());
        }
    },
    PUBLICACAO_A_DIVULGAR_POR_EMPRESA("BoletimInformativo.findUltimoADivulgar", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("tipo", TipoBoletimInformativo.PUBLICACAO).
                    set("statusBoletim", StatusBoletimInformativo.PUBLICADO).
                    set("data", DateUtil.getDataAtual());
        }
    },
    REMOVE_EVENTO_CALENDARIO_POR_EMPRESA("EventoCalendario.removeDesatualizadosPorEmpresa", "empresa", "limite"),
    COUNT_EVENTOS_CALENDARIO_EMPRESA("EventoCalendario.countByEmpresa", "empresa"),
    EVENTOS_CALENDARIO_EMPRESA("EventoCalendario.findByEmpresa", COUNT_EVENTOS_CALENDARIO_EMPRESA, "empresa"){

        @Override
        protected int extractResultLimit(Object... args) {
            return (int) args[1];
        }
    },
    DOCUMENTO_A_DIVULGAR_POR_EMPRESA("Documento.findUltimoADivulgar", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("data", DateUtil.getDataAtual());
        }

    },
    QUANTIDADE_DISPOSITIVOS_BY_EMPRESA("EstatisticaDispositivo.quantidadeDispositivosEmpresa", "empresa") {
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("tipos", Arrays.asList(
                            TipoDispositivo.ANDROID,
                            TipoDispositivo.IPHONE,
                            TipoDispositivo.ANDROID_FIREBASE,
                            TipoDispositivo.IPHONE_FIREBASE
                    ))
                    .set("limite", DateUtil.decrementaMeses(new Date(), 1));
        }
    },
    ESTATISTICAS_DISPOSITIVOS_BY_EMPRESA("EstatisticaDispositivo.findByEmpresa", "empresa"),
    ESTATISTICAS_ACESSO_BY_EMPRESA_AND_FUNCIONALIDADE("EstatisticaAcesso.findByEmpresaAndFuncionalidade", "empresa", "funcionalidade"),
    ESTATISTICAS_DISPOSITIVOS_ONLINE("EstatisticaDispositivo.findOnLine") {
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("tipos", Arrays.asList(
                            TipoDispositivo.ANDROID,
                            TipoDispositivo.IPHONE,
                            TipoDispositivo.ANDROID_FIREBASE,
                            TipoDispositivo.IPHONE_FIREBASE
                    ))
                    .set("limite", DateUtil.getDataAtualPrimeiraHora())
                    .set("statusEmpresa", StatusEmpresa.ATIVO);
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
                    .set("statusEmpresa", StatusEmpresa.ATIVO);
        }
    },
    REMOVE_ESTATISTICAS_DISPOSITIVOS_EMPRESAS_ANTIGAS("EstatisticaDispositivo.removeAntigas"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("limite", DateUtil.decrementaMeses(new Date(), 1));
        }
    },
    REMOVE_ESTATISTICAS_ACESSO_EMPRESAS_ANTIGAS("EstatisticaAcesso.removeAntigas"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("limite", DateUtil.decrementaMeses(new Date(), 1));
        }
    },
    REMOVE_REGISTROS_ACESSO_EMPRESAS_ANTIGAS("RegistroAcesso.removeAntigas"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("limite", DateUtil.decrementaDia(new Date(), 7));
        }
    },
    ITENS_PERIODO_CALENDARIO("ItemEvento.findByPeriodo", "chaveEmpresa", "dataInicio", "dataTermino"){
        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args)
                    .set("status", StatusItemEvento.PUBLICADO);
        }
    },
    VIDEOS_ANTIGOS("Video.findVideoNaoSincronizados", "empresa", "dataAtualizacao"),
    VIDEOS_EMPRESA("Video.findByEmpresa", "empresa"),
    GALERIA_FOTOS_ANTIGOS("GaleriaFotos.findNaoSincronizados", "empresa", "sincronizacao"),
    COUNT_GALERIA_FOTOS_EMPRESA("GaleriaFotos.countByEmpresa", "empresa"),
    GALERIA_FOTOS_EMPRESA("GaleriaFotos.findByEmpresa", COUNT_GALERIA_FOTOS_EMPRESA, "empresa");

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
