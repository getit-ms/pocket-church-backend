/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.calvinista.dto.FiltroDispositivoDTO;
import br.gafs.query.AbstractQuery;
import br.gafs.query.Queries;
import br.gafs.util.date.DateUtil;

/**
 *
 * @author Gabriel
 */
public class RegisterSentNotifications extends AbstractQuery implements Queries.NativeQuery{

    public RegisterSentNotifications(Long notification, FiltroDispositivoDTO filtro) {
        StringBuilder from = new StringBuilder(" from tb_dispositivo d inner join tb_preferencias p on d.chave = p.chave_dispositivo ");
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
        
        if (filtro.getHora() != null){
            where.append(" and p.deseja_receber_versiculos_diarios = true and p.hora_versiculo_diario = ").append(filtro.getHora().ordinal());
        }
        
        if (filtro.getMembro() != null){
            where.append(" and d.id_membro = ").append(filtro.getMembro()).append(" and d.status in (0, 1)");
        }

        if (filtro.getAniversario() != null){
            from.append(" inner join vw_aniversario_membro am on and am.id_membro = d.id_membro and am.chave_igreja = d.chave_igreja");
            where.append(" and am.dia = ").append(DateUtil.getDia(filtro.getAniversario())).append(" and am.mes = ").append(DateUtil.getMes(filtro.getAniversario()));
        }
        
        setQuery(new StringBuilder(" insert into tb_sent_notification(chave_dispositivo, id_membro, chave_igreja, id_notificacao_schedule, lido) select d.chave, d.id_membro, d.chave_igreja, ").
                append(notification).append(", false ").append(from).append(where).append(" group by d.chave, d.id_membro, d.chave_igreja").toString());
    }
    
}
