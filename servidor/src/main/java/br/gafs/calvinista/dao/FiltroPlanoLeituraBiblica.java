/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroEventoDTO;
import br.gafs.calvinista.dto.FiltroPlanoLeituraBiblicaDTO;
import br.gafs.dao.QueryParameters;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class FiltroPlanoLeituraBiblica extends AbstractPaginatedFiltro<FiltroPlanoLeituraBiblicaDTO> {

    public FiltroPlanoLeituraBiblica(String igreja, boolean admin, FiltroPlanoLeituraBiblicaDTO filtro) {
        super(filtro);
        
        StringBuilder from = new StringBuilder(" from PlanoLeituraBiblica plb");
        StringBuilder where = new StringBuilder(" where plb.igreja.chave = :chaveIgreja");
        Map<String, Object> args = new QueryParameters("chaveIgreja", igreja);

        if (admin){
            if (filtro.getDataInicio() != null){
                from.append(", DiaLeituraBiblica dlbdi");
                where.append(" and dlbdi.plano.id = plb.id and dlbdi.plano.igreja.chave = plb.igreja.chave and dlbdi.data >= :dataHoraInicio");
                args.put("dataHoraInicio", DateUtil.getDataPrimeiraHoraDia(filtro.getDataInicio()));
            }

            if (filtro.getDataTermino()!= null){
                from.append(", DiaLeituraBiblica dlbdt");
                where.append(" and dlbdt.plano.id = plb.id and dlbdt.plano.igreja.chave = plb.igreja.chave and dlbdt.data <= :dataHoraTermino");
                args.put("dataHoraTermino", DateUtil.getDataUltimaHoraDia(filtro.getDataTermino()));
            }
        }else{
                from.append(", DiaLeituraBiblica dlbdi, DiaLeituraBiblica dlbdt");
                where.append(" and dlbdi.plano.id = plb.id and dlbdi.plano.igreja.chave = plb.igreja.chave and and dlbdi.data >= :dataHoraInicio")
                        .append(" and dlbdt.plano.id = plb.id and dlbdt.plano.igreja.chave = plb.igreja.chave and dlbdt.data <= :dataHoraTermino");
                args.put("dataHoraInicio", DateUtil.getDataPrimeiraHoraDia(DateUtil.getDataAtual()));
                args.put("dataHoraTermino", DateUtil.getDataUltimaHoraDia(DateUtil.getDataAtual()));
        }

        setArguments(args);
        setPage(filtro.getPagina());
        setQuery(new StringBuilder("select plb ").append(from).append(where).append(" group by plb order by plb.descricao").toString());
        setCountQuery(QueryUtil.create(Queries.SingleCustomQuery.class, 
                new StringBuilder("select count(distinct plb.id) ").append(from).append(where).toString(), args));
        setResultLimit(filtro.getTotal());
    }
    
    
}
