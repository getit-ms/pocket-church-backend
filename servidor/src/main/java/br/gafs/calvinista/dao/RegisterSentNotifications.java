/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dao;

import br.gafs.query.AbstractQuery;
import br.gafs.query.Queries.NativeQuery;

/**
 *
 * @author Gabriel
 */
public class RegisterSentNotifications extends AbstractQuery implements NativeQuery {

    public RegisterSentNotifications(Long notification, String instalationsId) {
        StringBuilder from = new StringBuilder(" from tb_dispositivo d ");
        
        StringBuilder where = new StringBuilder(" where d.pushkey = '").append(instalationsId).append("'");

        setQuery(new StringBuilder(" insert into tb_sent_notification(chave_dispositivo, id_membro, chave_igreja, id_notificacao_schedule, lido) select d.chave, d.id_membro, d.chave_igreja, ").
                append(notification).append(", false ").append(from).append(where).append(" group by d.chave, d.id_membro, d.chave_igreja").toString());
    }
    
}
