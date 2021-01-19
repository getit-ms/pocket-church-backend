/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroEmailDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroEmail extends AbstractPaginatedFiltro<FiltroEmailDTO>{

    public FiltroEmail(FiltroEmailDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder(" from Colaborador m");
        StringBuilder where = new StringBuilder(" where m.empresa.chave = :chaveEmpresa ");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", filtro.getEmpresa().getChave());

        if (filtro.getColaborador() != null){
            where.append(" and m.id = :colaborador");
            args.put("colaborador", filtro.getColaborador());
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select m.email ").append(from).append(where).append(" group by m.email").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(m.email) ").append(from).append(where).toString(), args));
        setResultLimit(500);
    }
    
}
