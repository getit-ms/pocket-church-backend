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
        
        StringBuilder query = new StringBuilder("from SentNotification sn inner join sn.igreja i inner join sn.dispositivo d left join sn.membro m where i.chave = :igreja");
        Map<String, Object> args = new QueryParameters("igreja", igreja);
        
        if (membro != null){
            query.append(" and ((d.chave = :dispositivo and m.id is null) or m.id = :membro)");
            args.put("dispositivo", dispositivo);
            args.put("membro", membro);
        }else{
            query.append(" and d.chave = :dispositivo and m.id is null");
            args.put("dispositivo", dispositivo);
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select sn.notification.notificacao ").append(query).append(" group by sn.notification order by sn.notification.data desc").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(distinct sn) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
