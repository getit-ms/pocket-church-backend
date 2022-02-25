/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroVersiculoDiarioDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;

import java.util.Map;

/**
 * @author Gabriel
 */
public class FiltroVersiculoDiario extends AbstractPaginatedFiltro<FiltroVersiculoDiarioDTO> {

    public FiltroVersiculoDiario(String igreja, FiltroVersiculoDiarioDTO filtro) {
        super(filtro);

        StringBuilder from = new StringBuilder("from VersiculoDiario vd");
        StringBuilder where = new StringBuilder(" where vd.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);


        if (!StringUtil.isEmpty(filtro.getFiltro())) {
            where.append(" and upper(vd.versiculo) like :filtro");
            args.put("filtro", "%" + filtro.getFiltro().toUpperCase() + "%");
        }

        StringBuilder query = from.append(where);

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select vd ").append(query).append(" order by vd.versiculo").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(vd) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
