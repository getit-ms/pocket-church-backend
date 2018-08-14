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

    public FiltroChamado(String igreja, String dispositivo, Long membro, boolean admin, FiltroChamadoDTO filtro) {
        super(filtro);
        
        StringBuilder query = new StringBuilder("from Chamado c where c.igrejaSolicitante.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (admin){
            query.append(" and c.tipo = :tipo");
            args.put("tipo", TipoChamado.SUPORTE);
        }else{
            query.append(" and c.tipo <> :tipo");
            args.put("tipo", TipoChamado.SUPORTE);
            
            if (membro != null){
                query.append(" and (c.membroSolicitante.id = :idMembro or (c.membroSolicitante.id is null and c.dispositivoSolicitante.chave = :chaveDispositivo))");
                args.put("idMembro", membro);
                args.put("chaveDispositivo", dispositivo);
            }else{
                query.append(" and c.dispositivoSolicitante.chave = :chaveDispositivo");
                args.put("chaveDispositivo", dispositivo);
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
