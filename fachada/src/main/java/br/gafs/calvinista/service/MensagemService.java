/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.service;

import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.NotificationSchedule;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Gabriel
 */
public interface MensagemService extends Serializable {
    void sendNow(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro);

    NotificationSchedule sendWhenPossible(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro);
    NotificationSchedule sendLater(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro, Date dataHora);
    void sendNow(MensagemEmailDTO notificacao, FiltroEmailDTO filtro);

    NotificationSchedule sendWhenPossible(MensagemEmailDTO notificacao, FiltroEmailDTO filtro);
    NotificationSchedule sendLater(MensagemEmailDTO notificacao, FiltroEmailDTO filtro, Date dataHora);
    void enviarMensagem(ContatoDTO contato);

    Long countNotificacoesNaoLidas();
    Long countNotificacoesNaoLidas(String chaveIgreja, String chaveDispositivo, Long idMembro);
    void marcaNotificacoesComoLidas(String chaveIgreja, String chaveDispositivo, Long idMembro);
}
