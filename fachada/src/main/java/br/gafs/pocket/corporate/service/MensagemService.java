/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.service;

import br.gafs.pocket.corporate.dto.ContatoDTO;
import br.gafs.pocket.corporate.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.pocket.corporate.dto.FiltroEmailDTO;
import br.gafs.pocket.corporate.dto.MensagemEmailDTO;
import br.gafs.pocket.corporate.dto.MensagemPushDTO;
import br.gafs.pocket.corporate.entity.NotificationSchedule;

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
    Long countNotificacoesNaoLidas(String chaveEmpresa, String chaveDispositivo, Long idColaborador);
    void marcaNotificacoesComoLidas(String chaveEmpresa, String chaveDispositivo, Long idColaborador);
}
