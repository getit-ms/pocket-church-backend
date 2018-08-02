/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroBoletimDTO;
import br.gafs.pocket.corporate.entity.domain.StatusBoletimInformativo;
import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroBoletim extends AbstractPaginatedFiltro<FiltroBoletimDTO> {

    public FiltroBoletim(String empresa, boolean admin, FiltroBoletimDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from BoletimInformativo b where b.empresa.chave = :chaveEmpresa and b.tipo = :tipo");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa).set("tipo", filtro.getTipo());

        if (!admin){
            query.append(" and b.status = :status and b.dataPublicacao <= :dataCorte");
            args.put("status", StatusBoletimInformativo.PUBLICADO);
            args.put("dataCorte", DateUtil.getDataAtual());
        }

        String orderBy;
        if (filtro.getTipo() == TipoBoletimInformativo.BOLETIM) {
            orderBy = "b.data desc, b.dataPublicacao desc, b.titulo";
        } else {
            orderBy = "b.titulo";
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select b ").append(query).append(" order by ").append(orderBy).toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(b) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
