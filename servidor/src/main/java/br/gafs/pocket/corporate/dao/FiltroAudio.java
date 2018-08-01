/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroAudioDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroAudio extends AbstractPaginatedFiltro<FiltroAudioDTO>{

    public FiltroAudio(String empresa, FiltroAudioDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Audio a where a.empresa.chave = :chaveEmpresa");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa);
        
        if (filtro.getCategoria() != null) {
            query.append(" and a.categoria.id = :categoria");
            args.put("categoria", filtro.getCategoria());
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select a ").append(query).append(" order by a.nome").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(a) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
