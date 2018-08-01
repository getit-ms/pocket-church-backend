/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.pocket.corporate.dto.FiltroNoticiaDTO;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroNoticia extends AbstractPaginatedFiltro<FiltroNoticiaDTO>{

    public FiltroNoticia(String empresa, boolean admin, FiltroNoticiaDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Noticia n where n.empresa.chave = :chaveEmpresa");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa);
        
        if (!admin){
            query.append(" and n.dataPublicacao <= :dataCorte");
            args.put("dataCorte", DateUtil.getDataAtual());
        }

        if (filtro.getTipo() != null) {
            query.append(" and n.tipo = :tipo");
            args.put("tipo", filtro.getTipo());
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select n ").append(query).append(" order by n.dataPublicacao desc, n.titulo").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(n) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
