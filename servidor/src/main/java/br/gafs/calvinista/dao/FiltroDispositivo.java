/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroDispositivoDTO;
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
public class FiltroDispositivo extends AbstractPaginatedFiltro<FiltroDispositivoDTO>{

    public FiltroDispositivo(FiltroDispositivoDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder(" from Dispositivo d inner join d.preferencias p");
        StringBuilder where = new StringBuilder(" where d.igreja.chave = :chaveIgreja and d.pushkey <> 'unknown'");
        Map<String, Object> args = new QueryParameters("chaveIgreja", filtro.getIgreja().getChave());
        
        if (filtro.getMinisterios() != null && !filtro.getMinisterios().isEmpty()){
            from.append(" inner join p.ministeriosInteresse mi");
            where.append(" and mi.id in :ministerios");
            args.put("ministerios", filtro.getMinisterios());
        }
        
        if (filtro.getTipo() != null){
            where.append(" and d.tipo = :tipo");
            args.put("tipo", filtro.getTipo());
        }
        
        if (filtro.getHora() != null){
            where.append(" and p.desejaReceberVersiculosDiarios = true and p.horaVersiculoDiario = :hora");
            args.put("hora", filtro.getHora());
        }
        
        if (filtro.getMembro() != null){
            where.append(" and d.membro.id = :membro and d.membro.status in :statusMembro");
            args.put("membro", filtro.getMembro());
            args.put("statusMembro", Arrays.asList(StatusMembro.CONTATO, StatusMembro.MEMBRO));
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select d.pushkey ").append(from).append(where).append(" group by d.pushkey").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(d.pushkey) ").append(from).append(where).toString(), args));
        setResultLimit(500);
    }
    
}
