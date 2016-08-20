/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroCifraDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;

import java.util.Map;

public class FiltroCifra extends AbstractPaginatedFiltro<FiltroCifraDTO> {

    public FiltroCifra(String igreja, FiltroCifraDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Cifra c where c.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);
        
        if (!StringUtil.isEmpty(filtro.getFiltro())){
            query.append(" and (lower(concat(c.titulo, c.autor, c.letra)) like :filtro)");
            args.put("filtro", "%" + filtro.getFiltro().toLowerCase() + "%");
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select c ").append(query).append(" order by c.titulo").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(c) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
