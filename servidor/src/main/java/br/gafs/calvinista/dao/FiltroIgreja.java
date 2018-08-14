/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroIgrejaDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.AbstractQuery;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroIgreja extends AbstractQuery implements Queries.PaginatedCustomQuery {

    public FiltroIgreja(FiltroIgrejaDTO filtro) {
        StringBuilder query = new StringBuilder("from Igreja i where 1=1 ");
        Map<String, Object> args = new QueryParameters();
        
        if (!StringUtil.isEmpty(filtro.getChave())){
            query.append(" and upper(i.chave) like :chave");
            args.put("chave", filtro.getChave().toUpperCase());
        }
        
        if (!StringUtil.isEmpty(filtro.getNome())){
            query.append(" and upper(i.nome) like :nome");
            args.put("nome", filtro.getNome().toUpperCase());
        }
        
        setArguments(args);
        setQuery(new StringBuilder("select i ").append(query).append(" order by i.chave").toString());
        setPage(filtro.getPagina());
        setResultLimit(filtro.getTotal());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(i) ").append(query).toString(), args));
    }

}
