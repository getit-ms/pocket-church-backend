/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroMensagemDiaDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroMensagemDia extends AbstractPaginatedFiltro<FiltroMensagemDiaDTO>{

    public FiltroMensagemDia(String empresa, FiltroMensagemDiaDTO filtro) {
        super(filtro);

        StringBuilder from = new StringBuilder("from MensagemDia vd");
        StringBuilder where = new StringBuilder(" where vd.empresa.chave = :chaveEmpresa");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa);
        
        
        if (!StringUtil.isEmpty(filtro.getFiltro())){
            where.append(" and upper(vd.mensagem) like :filtro");
            args.put("filtro", "%" + filtro.getFiltro().toUpperCase() + "%");
        }
        
        StringBuilder query = from.append(where);
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select vd ").append(query).append(" order by vd.mensagem").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(vd) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
