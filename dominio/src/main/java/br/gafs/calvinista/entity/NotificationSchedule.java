/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;

/**
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

    @JsonIgnore
    @Column(name = "enviado", nullable = false)
    private boolean enviado = false;

    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "notificacao_json", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String notificacao;

    @JsonIgnore
    @Column(name = "to_json", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String to;

    public NotificationSchedule(NotificationType type, Date data, String notificacao, String to) {
        this.type = type;
        this.data = data;
        this.notificacao = notificacao;
        this.to = to;
    }

    public void enviado() {
        enviado = true;
    }
}
