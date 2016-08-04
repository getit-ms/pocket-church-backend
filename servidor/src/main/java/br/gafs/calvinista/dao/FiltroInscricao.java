/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.dto.FiltroMinhasInscricoesDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.domain.StatusInscricaoEvento;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroInscricao extends AbstractPaginatedFiltro<FiltroInscricaoDTO> {

    public FiltroInscricao(Long evento, Igreja igreja, Membro membro, FiltroInscricaoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from InscricaoEvento ie where ie.evento.id = :evento and ie.evento.igreja.chave = :chaveIgreja and ie.status in :status");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja.getId()).set("evento", evento);
        
        if (filtro instanceof FiltroMinhasInscricoesDTO){
            query.append(" and ie.membro.id = :membro");
            args.put("membro", membro.getId());
            args.put("status", Arrays.asList(StatusInscricaoEvento.CONFIRMADA, StatusInscricaoEvento.PENDENTE, StatusInscricaoEvento.CANCELADA));
        }else{
            args.put("status", Arrays.asList(StatusInscricaoEvento.CONFIRMADA, StatusInscricaoEvento.PENDENTE));
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select ie ").append(query).append(" order by ie.data").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(ie) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
