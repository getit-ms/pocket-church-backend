/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroHinoDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;

import java.util.Date;
import java.util.Map;

/**
 * @author Gabriel
 */
public class FiltroHino extends AbstractPaginatedFiltro<FiltroHinoDTO> {

    public FiltroHino(String igreja, FiltroHinoDTO filtro) {
        super(filtro);

        StringBuilder query = new StringBuilder("from Hino h, Igreja i where h.locale = i.locale and i.chave = :igreja");
        Map<String, Object> args = new QueryParameters("igreja", igreja);
        String orderBy = " order by h.numero, h.nome";

        if (!StringUtil.isEmpty(filtro.getFiltro())) {
            query.append(" and (lower(concat(h.nome, h.numero, h.assunto, h.autor, h.texto)) like :filtro)");
            args.put("filtro", "%" + filtro.getFiltro().toLowerCase() + "%");
        }

        if (filtro.getUltimaAtualizacao() != null) {
            query.append(" and h.ultimaAlteracao >= :ultimaAlteracao");
            args.put("ultimaAlteracao", new Date(filtro.getUltimaAtualizacao().getTime() + 1));
            orderBy = " order by h.ultimaAlteracao desc, h.numero, h.nome";
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select h ").append(query).append(orderBy).toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(h) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
