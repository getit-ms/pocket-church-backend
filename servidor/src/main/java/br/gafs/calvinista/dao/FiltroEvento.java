/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroEvento extends AbstractPaginatedFiltro<FiltroEventoDTO> {

    public FiltroEvento(String igreja, boolean admin, FiltroEventoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Evento e where e.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (filtro.getTipo() != null){
            query.append(" and e.tipo = :tipo");
            args.put("tipo", filtro.getTipo());
        }
        
        if (admin){
            if (filtro.getDataInicio() != null){
                query.append(" and e.dataHoraInicio >= :dataHoraInicio");
                args.put("dataHoraInicio", filtro.getDataInicio());
            }

            if (filtro.getDataTermino()!= null){
                query.append(" and e.dataHoraTermino >= :dataHoraTermino");
                args.put("dataHoraTermino", filtro.getDataTermino());
            }
        }else{
            query.append(" and e.dataHoraTermino >= :dataHoraTermino");
            args.put("dataHoraTermino", DateUtil.getDataAtual());
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select e ").append(query).append(" order by e.dataHoraInicio").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(e) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
