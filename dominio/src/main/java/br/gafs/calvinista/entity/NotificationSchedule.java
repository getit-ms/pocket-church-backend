/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.NotificationType;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tb_notification_schedule")
@NamedQueries({
    @NamedQuery(name = "NotificationSchedule.findNaoEnviadosByDataAndType", query = "select ns from NotificationSchedule ns where ns.enviado = false and ns.type = :type and ns.data <= CURRENT_TIMESTAMP")
})
public class NotificationSchedule implements IEntity {
    @Id
    @Column(name = "id_notificacao_schedule")
    @SequenceGenerator(sequenceName = "seq_notificacao_schedule", name = "seq_notificacao_schedule")
    @GeneratedValue(generator = "seq_notificacao_schedule", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NonNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data", nullable = false, updatable = false)
    private Date data;
    
    @Column(name = "enviado", nullable = false)
    private boolean enviado = false;
    
    @ManyToOne
    @JoinColumn(name = "chave_igreja", nullable = false, updatable = false)
    private Igreja igreja;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "notificacao_json", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String notificacao;
    
    @Column(name = "to_json", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String to;

    public NotificationSchedule(NotificationType type, Date data, Igreja igreja, String notificacao, String to) {
        this.type = type;
        this.data = data;
        this.igreja = igreja;
        this.notificacao = notificacao;
        this.to = to;
    }
    
    public void enviado(){
        enviado = true;
    }
}
