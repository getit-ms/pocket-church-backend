/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.calvinista.entity.domain.StatusEvento;
import br.gafs.calvinista.entity.domain.StatusInscricaoEvento;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
@Getter
@Setter
public class FiltroEvento implements Queries.PaginatedNativeQuery {
    private String query;
    private Queries.SingleNativeQuery countQuery;
    private Map<String, Object> arguments;
    private int page;
    private int resultLimit;

    public FiltroEvento(String igreja, boolean admin, FiltroEventoDTO filtro) {
        String fromCount = "from tb_evento e";
        String from = fromCount + " left join tb_inscricao_evento ie on e.id_evento = ie.id_evento and e.chave_igreja = ie.chave_igreja and ie.status in (#statusInscricaoConfirmada, #statusInscricaoPendente)";

        StringBuilder where = new StringBuilder(" where e.chave_igreja = #chaveIgreja and e.status = #status");
        Map<String, Object> argsCount = new QueryParameters("chaveIgreja", igreja).set("status", StatusEvento.ATIVO.ordinal());
        Map<String, Object> args = new QueryParameters()
                .set("statusInscricaoPendente", StatusInscricaoEvento.PENDENTE.ordinal())
                .set("statusInscricaoConfirmada", StatusInscricaoEvento.CONFIRMADA.ordinal());

        if (filtro.getTipo() != null){
            where.append(" and e.tipo = #tipo");
            argsCount.put("tipo", filtro.getTipo().ordinal());
        }
        
        if (admin){
            if (filtro.getDataInicio() != null){
                where.append(" and e.data_hora_inicio >= #dataHoraInicio");
                argsCount.put("dataHoraInicio", filtro.getDataInicio());
            }

            if (filtro.getDataTermino()!= null){
                where.append(" and e.data_hora_termino >= #dataHoraTermino");
                argsCount.put("dataHoraTermino", filtro.getDataTermino());
            }
        }else{
            where.append(" and e.data_hora_termino >= #dataHoraTermino");
            argsCount.put("dataHoraTermino", DateUtil.getDataAtual());
        }

        args.putAll(argsCount);

        StringBuilder select = new StringBuilder("select e.id_evento, e.nome, e.data_hora_inicio, e.data_hora_termino, e.data_inicio_inscricao, e.data_fim_inscricao, e.limite_inscricoes, count(ie.id_inscricao) ");
        String groupBy = " group by e.id_evento, e.nome, e.data_hora_inicio, e.data_hora_termino, e.data_inicio_inscricao, e.data_fim_inscricao, e.limite_inscricoes";

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(select.append(from).append(where).append(groupBy).append(" order by e.nome, e.data_hora_inicio").toString());
        setCountQuery(QueryUtil.create(Queries.SingleNativeQuery.class, new StringBuilder("select count(*) ").append(fromCount).append(where).toString(), argsCount));
        setResultLimit(filtro.getTotal());
    }
    
}
