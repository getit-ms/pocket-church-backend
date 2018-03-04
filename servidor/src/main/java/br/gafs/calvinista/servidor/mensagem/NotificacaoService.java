/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.dao.DAOService;

import javax.ejb.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class NotificacaoService {

    private static final Logger LOGGER = Logger.getLogger(NotificacaoService.class.getName());

    @EJB
    private DAOService daoService;

    @EJB
    private NotificacaoDispatcherService dispatcher;

    @Asynchronous
    public void enviaNotificacoes(Long idNotificacao, FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push) {
        LOGGER.log(Level.SEVERE, "enviaNotificacoes inicido para: " + idNotificacao);

        Long quantidade = daoService.findWith(new FiltroDispositivoNotificacao(filtro).getCountQuery());

        LOGGER.log(Level.SEVERE, "Quantidade total de dispositivos a enviar: " + quantidade);

        for (int i=0;i<quantidade;i+=FiltroDispositivoNotificacao.RESLTA_LIMIT) {

            filtro = filtro.clone();

            filtro.setPagina((i/FiltroDispositivoNotificacao.RESLTA_LIMIT) + 1);

            dispatcher.enviaNotificacoes(idNotificacao, filtro, push);
        }
    }
}
