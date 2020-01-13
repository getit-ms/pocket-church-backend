/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroColaboradorDTO;
import br.gafs.pocket.corporate.entity.domain.StatusColaborador;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroColaborador extends AbstractPaginatedFiltro<FiltroColaboradorDTO> {

    public FiltroColaborador(boolean admin, String empresa, FiltroColaboradorDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder("from Colaborador m");
        StringBuilder where = new StringBuilder(" where m.empresa.chave = :chaveEmpresa and m.status in :status");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa).
                set("status", Arrays.asList(StatusColaborador.CONTATO, StatusColaborador.COLABORADOR));
        
        if (!admin){
            where.append(" and m.dadosDisponiveis = true");
        }

        if (filtro.isAcessoRecente()) {
            where.append(" abd exists (select d from Dispositivo d where d.colaborador = m and d.ultimoAcesso >= :limiteAcesso)");
            args.put("limiteAcesso", DateUtil.decrementaMeses(new Date(), 1));
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
