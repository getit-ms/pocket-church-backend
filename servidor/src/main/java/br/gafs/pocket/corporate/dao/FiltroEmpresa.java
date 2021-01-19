/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroEmpresaDTO;
import br.gafs.pocket.corporate.entity.domain.StatusEmpresa;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.string.StringUtil;

import java.util.Map;

public class FiltroEmpresa extends AbstractPaginatedFiltro<FiltroEmpresaDTO> {

    public FiltroEmpresa(FiltroEmpresaDTO filtro) {
        super(filtro);

        StringBuilder query = new StringBuilder("from Template temp, Institucional inst inner join inst.empresa i left join inst.enderecos edr on edr.cidade <> '' and edr.estado <> ''")
                .append(" where i = TEMP.empresa and i.status = :status");
        Map<String, Object> args = new QueryParameters("status", StatusEmpresa.ATIVO);

        if (!StringUtil.isEmpty(filtro.getChave())) {
            query.append(" and i.chave = :chave");
            args.put("chave", filtro.getChave());
        } else if (!StringUtil.isEmpty(filtro.getFiltro())){
            query.append(" and lower(concat(i.nome, i.nomeAplicativo, edr.cidade, edr.estado)) like :filtro");
            args.put("filtro", "%" + filtro.getFiltro().replace(" ", "%").toLowerCase() + "%");
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select new br.gafs.pocket.corporate.dto.ResumoEmpresaDTO(i.chave, i.nome, i.nomeAplicativo, temp.logoPequena, max(edr.cidade), max(edr.estado)) ")
                .append(query).append(" group by i.chave, i.nome, i.nomeAplicativo, temp.logoPequena order by i.nome").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class,
                new StringBuilder("select count(i) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
