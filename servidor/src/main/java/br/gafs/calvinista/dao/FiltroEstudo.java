/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroEstudoDTO;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.domain.StatusEstudo;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroEstudo extends AbstractPaginatedFiltro<FiltroEstudoDTO>{

    public FiltroEstudo(String igreja, boolean admin, FiltroEstudoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Estudo e where e.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);
        
        if (!admin){
            query.append(" and e.dataPublicacao <= :dataCorte and e.status = :status");
            args.put("dataCorte", DateUtil.getDataAtual());
            args.put("status", StatusEstudo.PUBLICADO);
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
