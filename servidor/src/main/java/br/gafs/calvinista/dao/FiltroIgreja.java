/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroIgrejaDTO;
import br.gafs.calvinista.entity.domain.StatusIgreja;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;

import java.util.Map;

public class FiltroIgreja extends AbstractPaginatedFiltro<FiltroIgrejaDTO> {

    public FiltroIgreja(FiltroIgrejaDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Template temp, Institucional inst inner join inst.igreja i left join inst.enderecos edr on edr.cidade <> '' and edr.estado <> ''")
        .append(" where i = temp.igreja and i.status = :status");
        Map<String, Object> args = new QueryParameters("status", StatusIgreja.ATIVO);

        if (!StringUtil.isEmpty(filtro.getChave())) {
            query.append(" and i.chave = :chave");
            args.put("chave", filtro.getChave());
        } else if (!StringUtil.isEmpty(filtro.getFiltro())){
            query.append(" and lower(i.nome) like :filtro");
            args.put("filtro", "%" + filtro.getFiltro().replace(" ", "%").toLowerCase() + "%");
        }

        if (!StringUtil.isEmpty(filtro.getAgrupamento())) {
            query.append(" and i.agrupamento = :agrupamento");
            args.put("agrupamento", filtro.getAgrupamento());
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select new br.gafs.calvinista.dto.ResumoIgrejaDTO(i.chave, i.nome, i.nomeAplicativo, temp.logoPequena, edr.cidade, edr.estado) ")
                .append(query).append("group by i.chave, i.nome, i.nomeAplicativo, temp.logoPequena, edr.cidade, edr.estado order by i.nome").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(i) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
