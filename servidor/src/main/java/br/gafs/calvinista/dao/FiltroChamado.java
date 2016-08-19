/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroChamadoDTO;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.domain.TipoChamado;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroChamado extends AbstractPaginatedFiltro<FiltroChamadoDTO> {

    public FiltroChamado(Dispositivo dispositivo, FiltroChamadoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Chamado c where c.igrejaSolicitante.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", dispositivo.getIgreja().getChave());

        if (dispositivo.isAdministrativo()){
            query.append(" and c.tipo = :tipo");
            args.put("tipo", TipoChamado.SUPORTE);
        }else{
            if (dispositivo.getMembro() != null){
                query.append(" and (c.membroSolicitante.id = :idMembro or (c.membroSolicitante.id is null and c.dispositivoSolicitante.chave = :chaveDispositivo))");
                args.put("idMembro", dispositivo.getMembro().getId());
                args.put("chaveDispositivo", dispositivo.getChave());
            }else{
                query.append(" and c.dispositivoSolicitante.chave = :chaveDispositivo");
                args.put("chaveDispositivo", dispositivo.getChave());
            }
        }
        
        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select c ").append(query).append(" order by c.dataSolicitacao desc").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(c) ").append(query).toString(), args));
        setResultLimit(filtro.getTotal());
    }

}
