/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.FiltroEmailDTO;
import br.gafs.calvinista.entity.domain.StatusMembro;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroEmail extends AbstractPaginatedFiltro<FiltroEmailDTO>{

    public FiltroEmail(FiltroEmailDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder(" from Membro m");
        StringBuilder where = new StringBuilder(" where m.igreja.chave = :chaveIgreja ");
        Map<String, Object> args = new QueryParameters("chaveIgreja", filtro.getIgreja().getChave());

        if (filtro.getMembro() != null){
            where.append(" and m.id = :membro");
            args.put("membro", filtro.getMembro());
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select m.email ").append(from).append(where).append(" group by m.email").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(m.email) ").append(from).append(where).toString(), args));
        setResultLimit(500);
    }
    
}
