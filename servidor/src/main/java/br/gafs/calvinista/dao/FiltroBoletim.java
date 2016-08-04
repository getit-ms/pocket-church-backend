/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroBoletimDTO;
import br.gafs.calvinista.entity.Dispositivo;
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

    public FiltroBoletim(Dispositivo dispositivo, FiltroBoletimDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Boletim b where b.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", dispositivo.getIgreja().getChave());
        
        if (!dispositivo.isAdministrativo()){
            query.append(" and b.dataPublicacao <= :dataCorte");
            args.put("dataCorte", DateUtil.getDataAtual());
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select b ").append(query).append(" order by b.dataPublicacao desc").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(b) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
