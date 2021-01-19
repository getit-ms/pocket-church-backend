/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroMeusAgendamentoDTO;
import br.gafs.pocket.corporate.entity.domain.StatusAgendamentoAtendimento;
import br.gafs.pocket.corporate.entity.domain.StatusColaborador;
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
public class FiltroMeusAgendamentos extends AbstractPaginatedFiltro<FiltroMeusAgendamentoDTO> {

    public FiltroMeusAgendamentos(String empresa, Long colaborador, FiltroMeusAgendamentoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from AgendamentoAtendimento aa, Colaborador m where aa.empresa.chave = :chaveEmpresa and aa.colaborador.status in :statusColaborador");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa).
                set("statusColaborador", Arrays.asList(StatusColaborador.CONTATO, StatusColaborador.COLABORADOR));
        
        query.append(" and ((m.gerente = true and aa.calendario.gerente = m) or (m.gerente = false and aa.colaborador = m)) and m.id = :idColaborador and m.empresa.chave = :chaveEmpresa");
        args.put("idColaborador", colaborador);
        
        query.append(" and aa.dataHoraInicio >= :dataAtual");
        args.put("dataAtual", DateUtil.getDataAtual());
        
        query.append(" and aa.status not in :status");
        args.put("status", Arrays.asList(StatusAgendamentoAtendimento.CANCELADO));
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select aa ").append(query).append(" order by aa.dataHoraInicio").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(aa) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
