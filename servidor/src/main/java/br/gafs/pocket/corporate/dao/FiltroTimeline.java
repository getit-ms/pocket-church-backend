/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.pocket.corporate.dto.FiltroBoletimDTO;
import br.gafs.pocket.corporate.dto.FiltroTimelineDTO;
import br.gafs.pocket.corporate.entity.domain.StatusBoletimInformativo;
import br.gafs.pocket.corporate.entity.domain.StatusComentarioItemEvento;
import br.gafs.pocket.corporate.entity.domain.StatusItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroTimeline extends AbstractPaginatedFiltro<FiltroTimelineDTO> {

    public FiltroTimeline(String empresa, Long colaborador, FiltroTimelineDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from ItemEvento ie where ie.empresa.chave = :chaveEmpresa")
                .append(" and ie.status = :status and ie.dataHoraPublicacao <= :dataHoraPublicacao");
        Map<String, Object> args = new QueryParameters("chaveEmpresa", empresa)
                .set("status", StatusItemEvento.PUBLICADO)
                .set("dataHoraPublicacao", DateUtil.getDataAtual());

        if (filtro.getAutor() != null) {
            query.append(" and ie.autor.id = :autor");
            args.put("autor", filtro.getAutor());
        } else if (filtro.isSemAutor()) {
            query.append(" and ie.autor is null");
        }

        if (!StringUtil.isEmpty(filtro.getFiltro())) {
            query.append(" and lower(ie.titulo) like :filtro");
            args.put("filtro", "%" + filtro.getFiltro().toLowerCase() + "%");
        }

        Map<String, Object> argsSelect = new HashMap<>(args);
        argsSelect.put("statusComentario", StatusComentarioItemEvento.PUBLICADO);
        argsSelect.put("colaborador", colaborador);

        setArguments(argsSelect);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select ie, (select c.dataHora from CurtidaItemEvento c where c.itemEvento = ie and c.colaborador.id = :colaborador), (select count(c) from CurtidaItemEvento c where c.itemEvento = ie), (select count(c) from ComentarioItemEvento c where c.itemEvento = ie and c.status = :statusComentario) ").append(query).append(" order by " +
                "ie.dataHoraPublicacao desc, ie.tipo, ie.id desc ").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(ie) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
