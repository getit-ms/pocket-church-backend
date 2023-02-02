/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroMeusAgendamentoDTO;
import br.gafs.calvinista.entity.domain.StatusAgendamentoAtendimento;
import br.gafs.calvinista.entity.domain.StatusMembro;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Gabriel
 */
public class FiltroMeusAgendamentos extends AbstractPaginatedFiltro<FiltroMeusAgendamentoDTO> {

    public FiltroMeusAgendamentos(String igreja, Long membro, FiltroMeusAgendamentoDTO filtro) {
        super(filtro);

        StringBuilder query = new StringBuilder("from AgendamentoAtendimento aa, Membro m where aa.igreja.chave = :chaveIgreja and aa.membro.status in :statusMembro");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja).
                set("statusMembro", Arrays.asList(StatusMembro.CONTATO, StatusMembro.MEMBRO));

        query.append(" and ((m.pastor = true and aa.calendario.pastor = m) or (m.pastor = false and aa.membro = m)) and m.id = :idMembro and m.igreja.chave = :chaveIgreja");
        args.put("idMembro", membro);

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
