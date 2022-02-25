/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroVotacaoAtivaDTO;
import br.gafs.calvinista.dto.FiltroVotacaoDTO;
import br.gafs.calvinista.entity.Membro;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroVotacao extends AbstractPaginatedFiltro<FiltroVotacaoDTO> {

    public FiltroVotacao(String igreja, Long membro, FiltroVotacaoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder(" from Votacao v where v.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (filtro instanceof FiltroVotacaoAtivaDTO) {
            query.append(" and v.dataInicio <= :data and (v.dataTermino >= :data or v.dataTermino >= :dataLimite)");
            args.put("data", filtro.getData());
            args.put("dataLimite", DateUtil.incrementaDias(filtro.getData(), -30));
        } else if (filtro.getData() != null){
            query.append(" and v.dataInicio <= :data and v.dataTermino >= :data");
            args.put("data", filtro.getData());
        }

        if (!StringUtil.isEmpty(filtro.getNome())) {
            query.append(" and lower(v.nome) like :nome");
            args.put("nome", "%" + filtro.getNome().toLowerCase() + "%");
        }

        setPage(filtro.getPagina());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(v) ").append(query).toString(), args));

        Map<String, Object> selectArgs = new HashMap<>(args);
        setQuery(new StringBuilder("select v, (select count(vo) from Voto vo where vo.membro.id = :membro and vo.votacao = v)").append(query).append(" order by v.dataInicio desc, v.nome").toString());
        selectArgs.put("membro", membro);
        setArguments(selectArgs);
        setResultLimit(filtro.getTotal());
    }
    
}
