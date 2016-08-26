/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.StatusAgendamentoAtendimento;
import br.gafs.calvinista.entity.domain.StatusMinisterio;
import br.gafs.calvinista.entity.domain.StatusIgreja;
import br.gafs.calvinista.entity.domain.StatusMembro;
import br.gafs.calvinista.entity.domain.StatusVersiculoDiario;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Gabriel
 */
public enum QueryAcesso {
    AUTENTICA_MEMBRO("Membro.autentica", "igreja", "email", "senha"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            QueryParameters arguments = super.extractArguments(args);
            arguments.put("status", Arrays.asList(StatusMembro.CONTATO, StatusMembro.MEMBRO));
            return arguments;
        }
        
    },
    MINISTERIOS_ATIVOS("Ministerio.findByIgrejaAndStatus", "chaveIgreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            QueryParameters params = super.extractArguments(args);
            params.put("status", StatusMinisterio.ATIVO);
            return params;
        }
        
    }, 
    AUTENTICA_USUARIO("Usuario.autentica", "login", "senha"), 
    USUARIO_POR_AUTENTICACAO("Usuario.findByAutenticacao", "autenticacao"), 
    FUNCIONALIDADES_MEMBRO("Membro.findFuncionalidadesAcesso", "membro", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).set("funcionalidadesAdmin", 
                    Funcionalidade.FUNCIONALIDADES_ADMINISTRATIVO);
        }
        
    },
    TODAS_FUNCIONALIDADES_ADMIN("Igreja.findFuncionalidadesInList", "igreja"){

        @Override
        protected QueryParameters extractArguments(Object... args) {
            return super.extractArguments(args).
                    set("list", Funcionalidade.FUNCIONALIDADES_ADMINISTRATIVO); 
        }
        
    };
    
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
