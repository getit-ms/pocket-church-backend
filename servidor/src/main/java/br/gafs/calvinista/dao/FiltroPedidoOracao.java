/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroMeusPedidoOracaoDTO;
import br.gafs.calvinista.dto.FiltroPedidoOracaoDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.exceptions.ServiceException;
import br.gafs.query.Queries;

import java.util.Map;

/**
 * @author Gabriel
 */
public class FiltroPedidoOracao extends AbstractPaginatedFiltro<FiltroPedidoOracaoDTO> {

    public FiltroPedidoOracao(Long membro, String igreja, FiltroPedidoOracaoDTO filtro) {
        super(filtro);

        StringBuilder from = new StringBuilder("from PedidoOracao po ");
        StringBuilder where = new StringBuilder(" where po.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (filtro.getDataInicio() != null) {
            where.append(" and po.dataSolicitacao >= :dataInicio");
            args.put("dataInicio", filtro.getDataInicio());
        }

        if (filtro.getDataTermino() != null) {
            where.append(" and po.dataSolicitacao <= :dataTermino");
            args.put("dataTermino", filtro.getDataTermino());
        }

        if (filtro.getStatus() != null && !filtro.getStatus().isEmpty()) {
            where.append(" and po.status in :status");
            args.put("status", filtro.getStatus());
        }

        if (filtro instanceof FiltroMeusPedidoOracaoDTO) {
            if (membro == null) {
                throw new ServiceException("mensagens.MSG-403");
            }

            where.append(" and po.solicitante.id = :idSolicitante");
            args.put("idSolicitante", membro);
        }

        StringBuilder query = from.append(where);

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select po ").append(query).append(" order by po.status, po.dataSolicitacao desc").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(po) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
