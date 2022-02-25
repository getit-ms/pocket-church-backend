/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.calvinista.dto.FiltroComentarioDTO;
import br.gafs.calvinista.entity.domain.StatusComentarioItemEvento;
import br.gafs.query.Queries;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroComentario extends AbstractPaginatedFiltro<FiltroComentarioDTO>{

    public FiltroComentario(String igreja, FiltroComentarioDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from ComentarioItemEvento e where e.igreja.chave = :chaveIgreja")
                .append(" and e.status = :status and e.itemEvento.id = :idItemEvento and e.itemEvento.tipo = :tipoItemEvento");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja)
                .set("status", StatusComentarioItemEvento.PUBLICADO)
                .set("idItemEvento", filtro.getIdItemEvento())
                .set("tipoItemEvento", filtro.getTipoItemEvento());
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select e ").append(query).append(" order by e.dataHora desc, e.id desc").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(e) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
