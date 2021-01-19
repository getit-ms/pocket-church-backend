/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroFolgaDTO;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroFolga extends AbstractPaginatedFiltro<FiltroFolgaDTO> {

    public FiltroFolga(Empresa empresa, Long calendario, FiltroFolgaDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from FolgaAtendimento fa inner join fa.calendario c inner join c.empresa i where c.id = :idCalendario and i.chave = :chaveEmpresa");
        Map<String, Object> args = new QueryParameters("idCalendario", calendario).set("chaveEmpresa", empresa.getChave());
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select fa ").append(query).toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, new StringBuilder("select count(fa) ").
                append(query).toString(), args));
        setResultLimit(12);
    }
    
}
