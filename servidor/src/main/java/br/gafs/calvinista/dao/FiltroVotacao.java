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
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroVotacao extends AbstractPaginatedFiltro<FiltroVotacaoDTO> {

    public FiltroVotacao(String igreja, Long membro, FiltroVotacaoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Votacao v where v.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);
        
        if (filtro instanceof FiltroVotacaoAtivaDTO){
            query.append(" and not exists (select vv from Voto vv where vv.igreja.chave = :chaveIgreja and vv.votacao.id = v.id and vv.membro.id = :membro)");
            args.put("membro", membro);
        }
        
        if (filtro.getData() != null){
            query.append(" and v.dataInicio <= :dataVotacao and v.dataTermino >= :dataVotacao");
            args.put("dataVotacao", filtro.getData());
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select v ").append(query).append(" order by v.dataInicio desc, v.nome").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(v) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
}
