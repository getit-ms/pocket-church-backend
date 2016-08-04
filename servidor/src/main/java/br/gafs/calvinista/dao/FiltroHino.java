/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroHinoDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroHino extends AbstractPaginatedFiltro<FiltroHinoDTO> {

    public FiltroHino(Igreja igreja, FiltroHinoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Hino h where h.locale = :locale");
        Map<String, Object> args = new QueryParameters("locale", igreja.getLocale());
        
        if (!StringUtil.isEmpty(filtro.getFiltro())){
            query.append(" and (lower(h.nome) like :filtro or lower(h.numero) like :filtro or lower(h.assunto) like :filtro or lower(h.autor) like :filtro)");
            args.put("filtro", "%" + filtro.getFiltro().toLowerCase() + "%");
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select h ").append(query).append(" order by h.numero, h.nome").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(h) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
