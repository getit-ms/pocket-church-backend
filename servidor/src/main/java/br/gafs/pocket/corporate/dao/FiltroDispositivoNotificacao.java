/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.pocket.corporate.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 *
 * @author Gabriel
 */
@Getter
@NoArgsConstructor
public class FiltroDispositivoNotificacao implements Queries.NativeQuery {
    public static int RESLTA_LIMIT = 500;

    private String query;
    private Queries.SingleNativeQuery countQuery;
    private int page;

    public static StringBuilder query(FiltroDispositivoNotificacaoDTO filtro, StringBuilder from){
        StringBuilder where = new StringBuilder(" where d.chave_empresa = '").append(filtro.getEmpresa().getChave()).append("' and d.pushkey <> 'unknown' and d.id_colaborador is not null");

        if (filtro.getLotacoes() != null && !filtro.getLotacoes().isEmpty()){
            from.append(" inner join rl_lotacao_notificacao ln on ln.chave_empresa = d.chave_empresa and ln.chave_dispositivo = d.chave");

            StringBuilder mins = new StringBuilder("(").append(filtro.getLotacoes().get(0));
            for (int i=1;i<filtro.getLotacoes().size();i++){
                mins.append(",").append(filtro.getLotacoes().get(i));
            }
            mins.append(")");
            where.append(" and ln.id_lotacao in ").append(mins);
        }


        if (filtro.isDesejaReceberNotificacoesVideos()){
            where.append(" and p.deseja_receber_notificacoes_videos = true");
        }

        if (filtro.getColaborador() != null || filtro.isApenasGerentes()) {
            from.append(" inner join tb_colaborador m on m.id_colaborador = d.id_colaborador and m.chave_empresa = d.chave_empresa");

            if (filtro.getColaborador() != null) {
                where.append(" and m.id_colaborador = ").append(filtro.getColaborador()).append(" and m.status in (0, 1)");
            }

            if (filtro.isApenasGerentes()) {
                where.append(" and m.gerente = true");
            }
        }

        if (filtro.getAniversario() != null){
            from.append(" inner join vw_aniversario_colaborador am on am.id_colaborador = d.id_colaborador and am.chave_empresa = d.chave_empresa");
            where.append(" and am.dia = ").append(DateUtil.getDia(filtro.getAniversario())).append(" and am.mes = ").append(DateUtil.getMes(filtro.getAniversario()));
        }

        return where;
    }

    public FiltroDispositivoNotificacao(FiltroDispositivoNotificacaoDTO filtro) {
        StringBuilder from = new StringBuilder(" from tb_dispositivo d inner join tb_preferencias p on d.chave = p.chave_dispositivo ");
        StringBuilder where = query(filtro, from);

        if (filtro.getTipo() != null){
            where.append(" and d.tipo = ").append(filtro.getTipo().ordinal());
        }

        page = filtro.getPagina();
        query = new StringBuilder("select min(d.tipo), d.pushkey, max(d.chave), max(d.id_colaborador) ").append(from).append(where).append(" group by d.pushkey order by d.pushkey").toString();
        countQuery = QueryUtil.create(Queries.SingleNativeQuery.class, new StringBuilder("select count(distinct d.pushkey) ").append(from).append(where).toString());
    }

    @Override
    public Map<String, Object> getArguments() {
        return null;
    }

    @Override
    public int getResultLimit() {
        return RESLTA_LIMIT;
    }
}


