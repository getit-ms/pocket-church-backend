/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
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
    @NamedNativeQuery(name = "SentNotification.migraDispositivo", query = "update tb_sent_notification set chave_dispositivo = #newDispositivo where chave_dispositivo = #oldDispositivo"),
})
@NamedQueries({
    @NamedQuery(name = "SentNotification.clearNotificacoesDispositivo", query = "delete from SentNotification sn where sn.empresa.chave = :empresa and sn.chaveDispositivo = :dispositivo and sn.colaborador is null and sn.idNotificacao not in :excecoes"),
    @NamedQuery(name = "SentNotification.clearNotificacoesColaborador", query = "delete from SentNotification sn where sn.empresa.chave = :empresa and sn.colaborador.id = :colaborador and sn.idNotificacao not in :excecoes"),
    @NamedQuery(name = "SentNotification.findNotificacaoDispositivo", query = "select sn from SentNotification sn where sn.notification.id = :notificacao and sn.empresa.chave = :empresa and sn.dispositivo.chave = :dispositivo and sn.colaborador.id is null"),
    @NamedQuery(name = "SentNotification.findNotificacaoColaborador", query = "select sn from SentNotification sn where sn.notification.id = :notificacao and sn.empresa.chave = :empresa and sn.colaborador.id = :colaborador"),
    @NamedQuery(name = "SentNotification.findNaoLidasDispositivo", query = "select sn from SentNotification sn left join sn.colaborador m where sn.dispositivo.chave = :dispositivo and m.id is null"),
    @NamedQuery(name = "SentNotification.findNaoLidasColaborador", query = "select sn from SentNotification sn where sn.empresa.chave = :empresa and sn.colaborador.id = :colaborador"),
    @NamedQuery(name = "SentNotification.countNaoLidosDispositivo", query = "select count(distinct sn.notification.id) from SentNotification sn inner join sn.empresa i inner join sn.dispositivo d left join sn.colaborador m where sn.lido = false and i.chave = :empresa and d.chave = :dispositivo and m.id is null"),
    @NamedQuery(name = "SentNotification.countNaoLidosColaborador", query = "select count(distinct sn.notification.id) from SentNotification sn inner join sn.empresa i inner join sn.dispositivo d left join sn.colaborador m where sn.lido = false and i.chave = :empresa and m.id = :colaborador")
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
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Colaborador colaborador;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;
    
    @ManyToOne
    @JoinColumn(name = "id_notificacao_schedule")
    private NotificationSchedule notification;
    
    @Column(name = "lido")
    private boolean lido;
    
    public void lido(){
        this.lido = true;
    }

    @Override
    public SentNotificationId getId() {
        return new SentNotificationId(dispositivo.getChave(), notification.getId());
    }
    
}
