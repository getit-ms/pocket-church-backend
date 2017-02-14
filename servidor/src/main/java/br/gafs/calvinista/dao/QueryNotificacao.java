/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;

/**
 *
 * @author Gabriel
 */
public enum QueryNotificacao {
    NOTIFICACOES_A_EXECUTAR("NotificationSchedule.findNaoEnviadosByDataAndType", "type"){

        @Override
        protected int extractResultLimit(Object... args) {
            return 50;
        }
        
    },
    DEVICES_POR_TIPO("Dispositivo.findPorTipoAndIgreja", "igreja", "tipo", "dispositivos"), 
    COUNT_NOTIFICACOES_NAO_LIDAS("SentNotification.countNaoLidos", "igreja", "dispositivo", "membro"), 
    CLEAR_NOTIFICACOES_DISPOSITIVO("SentNotification.clearNotificacoesDispositivo", "igreja", "dispositivo"), 
    NOTIFICACAO_DISPOSITIVO("SentNotification.findNotificacaoDispositivo", "notificacao", "igreja", "dispositivo"), 
    CLEAR_NOTIFICACOES_MEMBRO("SentNotification.clearNotificacoesMembro", "igreja", "membro"), 
    NOTIFICACAO_MEMBRO("SentNotification.findNotificacaoMembro", "notificacao", "igreja", "membro"), 
    NOTIFICACOES_NAO_LIDAS_DISPOSITIVO("SentNotification.findNaoLidasDispositivo", "igreja", "dispositivo"),
    NOTIFICACOES_NAO_LIDAS_MEMBRO("SentNotification.findNaoLidasMembro", "igreja", "membro");

    private final String query;
    private final String[] parameters;
    private final QueryNotificacao countQuery;

    private QueryNotificacao(String query, String... parameters) {
        this(query, (QueryNotificacao) null, parameters);
    }
    
    private QueryNotificacao(String query, QueryNotificacao countQuery, String... parameters) {
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

    private QueryNotificacao getCountQuery() {
        return countQuery;
    }
}
