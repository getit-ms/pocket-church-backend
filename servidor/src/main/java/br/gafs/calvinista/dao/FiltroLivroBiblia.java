/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroLivroBibliaDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;

import java.util.Date;
import java.util.Map;

/**
 * @author Gabriel
 */
public class FiltroLivroBiblia extends AbstractPaginatedFiltro<FiltroLivroBibliaDTO> {

    public FiltroLivroBiblia(String igreja, FiltroLivroBibliaDTO filtro) {
        super(filtro);

        StringBuilder query = new StringBuilder("from LivroBiblia lb, Igreja i where i.biblia = lb.biblia and i.chave = :igreja");
        Map<String, Object> args = new QueryParameters("igreja", igreja);
        String orderBy = " order by lb.testamento, lb.ordem, lb.id";

        if (filtro.getUltimaAtualizacao() != null) {
            query.append(" and lb.ultimaAtualizacao >= :ultimaAtualizacao");
            args.put("ultimaAtualizacao", new Date(filtro.getUltimaAtualizacao().getTime() + 1));
            orderBy = " order by lb.ultimaAtualizacao desc, lb.testamento, lb.ordem, lb.id";
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select lb ").append(query).append(orderBy).toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(lb) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
