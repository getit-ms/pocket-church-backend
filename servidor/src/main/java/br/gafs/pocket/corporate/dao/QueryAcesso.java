/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.StatusColaborador;
import br.gafs.query.Queries;

import java.util.Arrays;

/**
 *
 * @author Gabriel
 */
public enum QueryAcesso {
    AUTENTICA_COLABORADOR("Colaborador.autentica", "empresa", "email", "senha"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            QueryParameters arguments = super.extractArguments(args);
            arguments.put("status", Arrays.asList(StatusColaborador.CONTATO, StatusColaborador.COLABORADOR));
            return arguments;
        }
        
    },
    AUTENTICA_USUARIO("Usuario.autentica", "login", "senha"),
    USUARIO_POR_AUTENTICACAO("Usuario.findByAutenticacao", "autenticacao"), 
    FUNCIONALIDADES_COLABORADOR_APP("Colaborador.findFuncionalidadesAcessoApp", "colaborador", "empresa"),
    FUNCIONALIDADES_COLABORADOR_ADMIN("Colaborador.findFuncionalidadesAcessoAdmin", "colaborador", "empresa"),
    TODAS_FUNCIONALIDADES_ADMIN("Empresa.findFuncionalidadesInList", "empresa"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("list", Funcionalidade.FUNCIONALIDADES_ADMINISTRATIVO); 
        }
        
    }, 
    MIGRA_SENT_NOTIFICATIONS("SentNotification.migraDispositivo", "oldDispositivo", "newDispositivo"),
    UNREGISTER_OLD_DEVICES("Dispositivo.unregisterOldDevices", "pushkey", "chaveDispositivo");
    
    private final String query;
    private final String[] parameters;
    private final QueryAcesso countQuery;

    private QueryAcesso(String query, String... parameters) {
        this(query, (QueryAcesso) null, parameters);
    }
    
    private QueryAcesso(String query, QueryAcesso countQuery, String... parameters) {
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

    private QueryAcesso getCountQuery() {
        return countQuery;
    }
}
