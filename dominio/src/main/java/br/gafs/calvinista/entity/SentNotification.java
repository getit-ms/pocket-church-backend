/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import javax.persistence.*;

/**
 * @author Gabriel
 */
@Getter
@Entity
@IdClass(SentNotificationId.class)
@Table(name = "tb_sent_notification")
@NamedQueries({
        @NamedQuery(name = "SentNotification.clearNotificacoesDispositivo", query = "delete from SentNotification sn where sn.igreja.chave = :igreja and sn.chaveDispositivo = :dispositivo and sn.membro is null and sn.idNotificacao not in :excecoes"),
        @NamedQuery(name = "SentNotification.clearNotificacoesMembro", query = "delete from SentNotification sn where sn.igreja.chave = :igreja and sn.membro.id = :membro and sn.idNotificacao not in :excecoes"),
        @NamedQuery(name = "SentNotification.findNotificacaoDispositivo", query = "select sn from SentNotification sn where sn.notification.id = :notificacao and sn.igreja.chave = :igreja and sn.dispositivo.chave = :dispositivo and sn.membro.id is null"),
        @NamedQuery(name = "SentNotification.findNotificacaoMembro", query = "select sn from SentNotification sn where sn.notification.id = :notificacao and sn.igreja.chave = :igreja and sn.membro.id = :membro"),
        @NamedQuery(name = "SentNotification.findNaoLidasDispositivo", query = "select sn from SentNotification sn left join sn.membro m where sn.dispositivo.chave = :dispositivo and m.id is null"),
        @NamedQuery(name = "SentNotification.findNaoLidasMembro", query = "select sn from SentNotification sn where sn.igreja.chave = :igreja and sn.membro.id = :membro"),
        @NamedQuery(name = "SentNotification.countNaoLidosDispositivo", query = "select count(distinct sn.notification.id) from SentNotification sn inner join sn.igreja i inner join sn.dispositivo d left join sn.membro m where sn.lido = false and i.chave = :igreja and d.chave = :dispositivo and m.id is null"),
        @NamedQuery(name = "SentNotification.countNaoLidosMembro", query = "select count(distinct sn.notification.id) from SentNotification sn inner join sn.igreja i inner join sn.dispositivo d left join sn.membro m where sn.lido = false and i.chave = :igreja and m.id = :membro")
})
public class SentNotification implements IEntity {
    @Id
    @Column(name = "chave_dispositivo", insertable = false, updatable = false)
    private String chaveDispositivo;

    @Id
    @Column(name = "id_notificacao_schedule", insertable = false, updatable = false)
    private Long idNotificacao;

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
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @ManyToOne
    @JoinColumn(name = "id_notificacao_schedule")
    private NotificationSchedule notification;

    @Column(name = "lido")
    private boolean lido;

    public void lido() {
        this.lido = true;
    }

    @Override
    public SentNotificationId getId() {
        return new SentNotificationId(dispositivo.getChave(), notification.getId());
    }

}
