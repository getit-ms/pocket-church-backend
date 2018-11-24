/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroNotificacoesDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroNotificacoes extends AbstractPaginatedFiltro<FiltroNotificacoesDTO> {

    public FiltroNotificacoes(String igreja, String dispositivo, Long membro, FiltroNotificacoesDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder("from SentNotification sn inner join sn.igreja i left join sn.membro m inner join sn.dispositivo d");
        StringBuilder where = new StringBuilder(" where i.chave = :igreja");
        Map<String, Object> args = new QueryParameters("igreja", igreja);
        
        if (membro != null){
            where.append(" and m.id = :membro");
            args.put("dispositivo", dispositivo);
            args.put("membro", membro);
        }else{
            where.append(" and d.chave = :dispositivo and m.id is null");
            args.put("dispositivo", dispositivo);
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select sn.notification ").append(from).append(where).append(" group by sn.notification order by sn.notification.data desc").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(distinct sn) ").append(from).append(where).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
