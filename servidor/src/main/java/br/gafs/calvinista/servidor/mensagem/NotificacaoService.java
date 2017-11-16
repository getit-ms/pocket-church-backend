/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dao.RegisterSentNotifications;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.SentNotification;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.dao.DAOService;
import br.gafs.dao.QueryUtil;
import br.gafs.query.Queries;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Stateless
public class NotificacaoService {

    private static final int TIPO = 0;
    private static final int INSTALATION_ID = 1;
    private static final int BADGE = 2;

    @EJB
    private DAOService daoService;
    
    @EJB
    private AndroidNotificationService androidService;
    
    @EJB
    private IOSNotificationService iosService;
    
    @Asynchronous
    public void enviaNotificacoes(Long idNotificacao, FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push) throws Exception {
        FiltroDispositivoNotificacao filtroDispositivoNotificacao = new FiltroDispositivoNotificacao(filtro);

        List<Object[]> dispositivos = daoService.findWith(QueryUtil.create(Queries.NativeQuery.class,
                filtroDispositivoNotificacao.getQuery(),
                filtroDispositivoNotificacao.getArguments(),
                filtroDispositivoNotificacao.getResultLimit()));

        List<IOSNotificationService.Destination> destinationIOS = new ArrayList<>();
        List<AndroidNotificationService.Destination> destinationAndroid = new ArrayList<>();
        for (Object[] dispositivo : dispositivos) {
            if (dispositivo[TIPO].equals(TipoDispositivo.ANDROID.ordinal())) {
                destinationAndroid.add(new AndroidNotificationService.Destination((String) dispositivo[INSTALATION_ID], (Long) dispositivo[BADGE] + 1));
            }else if (dispositivo[TIPO].equals(TipoDispositivo.IPHONE.ordinal())){
                destinationIOS.add(new IOSNotificationService.Destination((String) dispositivo[INSTALATION_ID], (Long) dispositivo[BADGE] + 1));
            }
        }

        androidService.pushNotifications(filtro.getIgreja(), push, destinationAndroid);

        iosService.pushNotifications(filtro.getIgreja(), push, destinationIOS);

        for (Object[] dispositivo : dispositivos) {
            daoService.execute(new RegisterSentNotifications(idNotificacao, (String) dispositivo[INSTALATION_ID]));
        }
    }
}
