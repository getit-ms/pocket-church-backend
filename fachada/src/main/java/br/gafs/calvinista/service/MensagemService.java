/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.NotificationSchedule;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Gabriel
 */
public interface MensagemService extends Serializable {
    void sendNow(Igreja igreja, MensagemPushDTO notificacao, List<String> to);
    NotificationSchedule sendWhenPossible(Igreja igreja, MensagemPushDTO notificacao, List<String> to);
    NotificationSchedule sendLater(Igreja igreja, MensagemPushDTO notificacao, List<String> to, Date dataHora);
    
    void sendNow(Igreja igreja, MensagemEmailDTO notificacao, List<String> to);
    NotificationSchedule sendWhenPossible(Igreja igreja, MensagemEmailDTO notificacao, List<String> to);
    NotificationSchedule sendLater(Igreja igreja, MensagemEmailDTO notificacao, List<String> to, Date dataHora);
}
