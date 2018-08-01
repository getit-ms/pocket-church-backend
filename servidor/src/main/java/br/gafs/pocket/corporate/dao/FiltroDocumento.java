/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroDocumentoDTO;
import br.gafs.pocket.corporate.entity.domain.StatusDocumento;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroDocumento extends AbstractPaginatedFiltro<FiltroDocumentoDTO>{

    public FiltroDocumento(String empresa, boolean admin, FiltroDocumentoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Documento e where e.empresa.chave = :chaveEmpresa");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa);
        
        if (!admin){
            query.append(" and e.dataPublicacao <= :dataCorte and e.status = :status");
            args.put("dataCorte", DateUtil.getDataAtual());
            args.put("status", StatusDocumento.PUBLICADO);
        }

        if (filtro.getCategoria() != null) {
            query.append(" and e.categoria.id = :categoria");
            args.put("categoria", filtro.getCategoria());
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select e ").append(query).append(" order by e.dataPublicacao desc, e.titulo").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(e) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
