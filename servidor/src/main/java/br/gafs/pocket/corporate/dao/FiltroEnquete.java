/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroEnqueteAtivaDTO;
import br.gafs.pocket.corporate.dto.FiltroEnqueteDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroEnquete extends AbstractPaginatedFiltro<FiltroEnqueteDTO> {

    public FiltroEnquete(String empresa, Long colaborador, FiltroEnqueteDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder(" from Enquete v where v.empresa.chave = :chaveEmpresa");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa);

        if (filtro instanceof FiltroEnqueteAtivaDTO) {
            query.append(" and v.dataInicio <= :data and (v.dataTermino >= :data or v.dataTermino >= :dataLimite)");
            args.put("data", filtro.getData());
            args.put("dataLimite", DateUtil.incrementaDias(filtro.getData(), -30));
        } else if (filtro.getData() != null){
            query.append(" and v.dataInicio <= :data and v.dataTermino >= :data");
            args.put("data", filtro.getData());
        }
        
        setPage(filtro.getPagina());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(v) ").append(query).toString(), args));

        Map<String, Object> selectArgs = new HashMap<>(args);
        setQuery(new StringBuilder("select v, (select count(vo) from RespostaEnqueteColaborador vo where vo.colaborador.id = :colaborador and vo.enquete = v)").append(query).append(" order by v.dataInicio desc, v.nome").toString());
        selectArgs.put("colaborador", colaborador);
        setArguments(selectArgs);
        setResultLimit(filtro.getTotal());
    }
    
}
