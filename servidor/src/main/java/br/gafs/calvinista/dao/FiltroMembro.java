/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroMembroDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.StatusMembro;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroMembro extends AbstractPaginatedFiltro<FiltroMembroDTO> {

    public FiltroMembro(boolean admin, String igreja, FiltroMembroDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder("from Membro m");
        StringBuilder where = new StringBuilder(" where m.igreja.chave = :chaveIgreja and m.status in :status");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (admin && filtro.isPendentes()) {
            args.put("status", Collections.singletonList(StatusMembro.PENDENTE));
        } else {
            args.put("status", Arrays.asList(StatusMembro.CONTATO, StatusMembro.MEMBRO));
        }
        
        if (!admin){
            where.append(" and m.dadosDisponiveis = true");
        }

        if (!StringUtil.isEmpty(filtro.getFiltro())) {
            where.append(" and lower(concat(m.nome, m.email)) like :filtro");
            args.put("filtro", "%" + filtro.getFiltro().toLowerCase() + "%");
        }
        
        if (!StringUtil.isEmpty(filtro.getNome())){
            where.append(" and lower(m.nome) like :nome");
            args.put("nome", "%" + filtro.getNome().toLowerCase() + "%");
        }
        
        if (!StringUtil.isEmpty(filtro.getEmail())){
            where.append(" and lower(m.email) like :email");
            args.put("email", "%" + filtro.getEmail().toLowerCase() + "%");
        }
        
        if (filtro.getPerfis() != null && !filtro.getPerfis().isEmpty()){
            from.append(" inner join m.acesso a inner join a.perfis p");
            where.append(" and p.id in :perfis");
            args.put("perfis", filtro.getPerfis());
        }
        
        StringBuilder query = from.append(where);
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select m ").append(query).append(" order by m.nome, m.id").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(m) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

    
}
