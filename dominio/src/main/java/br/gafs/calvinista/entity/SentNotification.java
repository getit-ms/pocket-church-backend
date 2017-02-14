/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.Getter;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@IdClass(SentNotificationId.class)
@Table(name = "tb_sent_notification")
@NamedNativeQueries({
    @NamedNativeQuery(name = "SentNotification.bulkInsert", query = " select d.chave_dispositivo, d.id_membro, d.chave_igreja, #notification, false from tb_dispositivo d where d.push_key in #pushkeys"),
    @NamedNativeQuery(name = "SentNotification.findNaoLidasDispositivo", query = "select sn.id_notificacao_schedule tb_sent_notification set lido = true where chave_igreja = #igreja and chave_dispositivo = #dispositivo and id_membro is null"),
    @NamedNativeQuery(name = "SentNotification.findNaoLidasMembro", query = "select sn.id_notificacao_schedule, sn.chave_dispositivo tb_sent_notification sn set lido = true from (select chave_igreja, id_membro from tb_sent_notification  where chave_igreja = #igreja and id_membro = #membro order by chave_igreja, id_membro for update) upd where sn.chave_igreja = upd.chave_igreja and sn.id_membro = upd.id_membro"),
    @NamedNativeQuery(name = "SentNotification.clearNotificacoesDispositivo", query = "delete from tb_sent_notification sn where sn.chave_igreja = #igreja and sn.chave_dispositivo = #dispositivo and sn.id_membro is null"),
    @NamedNativeQuery(name = "SentNotification.findNotificacaoDispositivo", query = "select sn.id_notificacao_schedule from tb_sent_notification sn where sn.id_notificacao_schedule = #notificacao and sn.chave_igreja = #igreja and sn.chave_dispositivo = #dispositivo and sn.id_membro is null"),
    @NamedNativeQuery(name = "SentNotification.clearNotificacoesMembro", query = "delete from tb_sent_notification sn where sn.chave_igreja = #igreja and sn.id_membro = #membro"),
    @NamedNativeQuery(name = "SentNotification.findNotificacaoMembro", query = "select sn.id_notificacao_schedule, sn.chave_dispositivo from tb_sent_notification sn where sn.id_notificacao_schedule = #notificacao and sn.chave_igreja = #igreja and sn.id_membro = #membro")
})
@NamedQueries({
    @NamedQuery(name = "SentNotification.countNaoLidos", query = "select count(distinct sn.notification.id) from SentNotification sn inner join sn.igreja i inner join sn.dispositivo d left join sn.membro m where sn.lido = false and i.chave = :igreja and ((d.chave = :dispositivo and m.id is null) or m.id = :membro)"),
})
public class SentNotification implements IEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "chave_dispositivo")
    private Dispositivo dispositivo;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro membro;
    
    @ManyToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "id_notificacao_schedule")
    private NotificationSchedule notification;
    
    @Column(name = "lido")
    private boolean lido;

    @Override
    public SentNotificationId getId() {
        return new SentNotificationId(dispositivo.getChave(), notification.getId());
    }
    
}
