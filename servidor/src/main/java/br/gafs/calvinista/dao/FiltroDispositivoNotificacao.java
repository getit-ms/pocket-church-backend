/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;
import java.util.Map;
import lombok.Getter;

import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@NoArgsConstructor
public class FiltroDispositivoNotificacao implements Queries.PaginatedNativeQuery {
    private String query;
    private Queries.SingleNativeQuery countQuery;
    private int page;
    private int resultLimit = 500;

    public static StringBuilder query(FiltroDispositivoNotificacaoDTO filtro, StringBuilder from){
        StringBuilder where = new StringBuilder(" where d.chave_igreja = '").append(filtro.getIgreja().getChave()).append("' and d.pushkey <> 'unknown'");
        
        if (filtro.getMinisterios() != null && !filtro.getMinisterios().isEmpty()){
            from.append(" inner join rl_preferencias_ministerios mi on mi.chave_igreja = d.chave_igreja and mi.chave_dispositivo = d.chave");
            
            StringBuilder mins = new StringBuilder("(").append(filtro.getMinisterios().get(0));
            for (int i=1;i<filtro.getMinisterios().size();i++){
                mins.append(",").append(filtro.getMinisterios().get(i));
            }
            mins.append(")");
            where.append(" and mi.id_ministerio in ").append(mins);
        }

        if (filtro.isApenasMembros()){
            where.append(" and d.id_membro is not null");
        }
        
        if (filtro.isDesejaReceberNotificacoesVideos()){
            where.append(" and p.deseja_receber_notificacoes_videos = true");
        }
        
        if (filtro.getHora() != null){
            if (filtro.getIdPlanoLeiuraBiblica() != null){
                from.append(" inner join tb_opcao_leitura_biblica olb on olb.id_membro = p.id_membro and olb.chave_igreja = p.chave_igreja and old.termino is null");
                where.append(" and olb.id_plano_leitura_biblica = ").append(filtro.getIdPlanoLeiuraBiblica()).
                        append(" and p.deseja_receber_lembretes_leitura_biblica = true").
                        append(" and p.hora_lembrete_leitura = ").append(filtro.getHora().ordinal());
            }else{
                where.append(" and p.deseja_receber_versiculos_diarios = true and p.hora_versiculo_diario = ").append(filtro.getHora().ordinal());
            }
        }
        
        if (filtro.getMembro() != null){
            from.append(" inner join tb_membro m on m.id_membro = d.id_membro and m.chave_igreja = d.chave_igreja");
            where.append(" and m.id_membro = ").append(filtro.getMembro()).append(" and m.status in (0, 1)");
        }

        if (filtro.getAniversario() != null){
            from.append(" inner join vw_aniversario_membro am on am.id_membro = d.id_membro and am.chave_igreja = d.chave_igreja");
            where.append(" and am.dia = ").append(DateUtil.getDia(filtro.getAniversario())).append(" and am.mes = ").append(DateUtil.getMes(filtro.getAniversario()));
        }
        
        return where;
    }
    
    public FiltroDispositivoNotificacao(FiltroDispositivoNotificacaoDTO filtro) {
        StringBuilder from = new StringBuilder(" from tb_dispositivo d inner join tb_preferencias p on d.chave = p.chave_dispositivo left join tb_sent_notification sn on sn.chave_igreja = d.chave_igreja and ((d.chave = sn.chave_dispositivo and sn.id_membro is null) or sn.id_membro = d.id_membro) and sn.lido = false");
        StringBuilder where = query(filtro, from);
        
        if (filtro.getTipo() != null){
            where.append(" and d.tipo = ").append(filtro.getTipo().ordinal());
        }
        
        page = filtro.getPagina();
        query = new StringBuilder("select d.pushkey, count(distinct sn.id_notificacao_schedule) ").append(from).append(where).append(" group by d.pushkey").toString();
        countQuery = QueryUtil.create(Queries.SingleNativeQuery.class, new StringBuilder("select count(distinct d.pushkey) ").append(from).append(where).toString());
    }

    @Override
    public Map<String, Object> getCountArguments() {
        return null;
    }

    @Override
    public Map<String, Object> getArguments() {
        return null;
    }
    
}


