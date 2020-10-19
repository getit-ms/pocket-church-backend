/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.pocket.corporate.dto.FiltroBoletimDTO;
import br.gafs.pocket.corporate.dto.FiltroTimelineDTO;
import br.gafs.pocket.corporate.entity.domain.StatusBoletimInformativo;
import br.gafs.pocket.corporate.entity.domain.StatusItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroTimeline extends AbstractPaginatedFiltro<FiltroTimelineDTO> {

    public FiltroTimeline(String empresa, FiltroTimelineDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from ItemEvento ie where ie.empresa.chave = :chaveEmpresa")
                .append(" and ie.status = :status and ie.dataHora <= :data");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa)
                .set("status", StatusItemEvento.PUBLICADO)
                .set("dataHora", DateUtil.getDataAtual());
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select ie ").append(query).append(" order by ie.dataHora desc, ie.tipo, ie.id desc ").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(ie) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
