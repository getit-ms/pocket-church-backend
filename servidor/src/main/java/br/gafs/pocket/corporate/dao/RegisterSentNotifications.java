/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dao;

import br.gafs.query.AbstractQuery;
import br.gafs.query.Queries.NativeQuery;

import java.util.List;

/**
 *
 * @author Gabriel
 */
public class RegisterSentNotifications extends AbstractQuery implements NativeQuery {

    public RegisterSentNotifications(Long notification, List<String> pushkeys) {
        StringBuilder from = new StringBuilder(" from tb_dispositivo d ");

        StringBuilder where = new StringBuilder(" where d.pushkey in (");

        boolean first = true;
        for (String pushkey : pushkeys) {
            if (!first) {
                where.append(",");
            }

            where.append("'").append(pushkey).append("'");

            first = false;
        }

        where.append(") and not exists (select sn.chave_dispositivo from tb_sent_notification sn where sn.chave_dispositivo = d.chave and sn.id_notificacao_schedule = ").append(notification).append(")");

        setQuery(new StringBuilder(" insert into tb_sent_notification(chave_dispositivo, id_colaborador, chave_empresa, id_notificacao_schedule, lido) select d.chave, d.id_colaborador, d.chave_empresa, ").
                append(notification).append(", false ").append(from).append(where).append(" group by d.chave, d.id_colaborador, d.chave_empresa").toString());
    }
    
}
