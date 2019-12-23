package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroDevocionarioDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

import java.util.Map;

public class FiltroDevocionario extends AbstractPaginatedFiltro<FiltroDevocionarioDTO> {

    public FiltroDevocionario(String igreja, FiltroDevocionarioDTO filtro) {
        super(filtro);

        StringBuilder query = new StringBuilder("from DiaDevocionario dd where dd.igreja.chave = :chaveIgreja ");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (filtro.getStatus() != null) {
            query.append(" and dd.status = :status");
            args.put("status", filtro.getStatus());
        }

        if (filtro.getDataInicio() != null) {
            query.append(" and dd.data >= :dataInicio");
            args.put("dataInicio", DateUtil.getDataPrimeiraHoraDia(filtro.getDataInicio()));
        }

        if (filtro.getDataTermino() != null) {
            query.append(" and dd.data <= :dataTermino");
            args.put("dataTermino", DateUtil.getDataUltimaHoraDia(filtro.getDataTermino()));
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select dd ").append(query).append(" order by ").append(" dd.data").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(dd) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
}
