/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Gabriel
 */
@Stateless
public class NotificacaoService {
    
    @EJB
    private DAOService daoService;
    
    @EJB
    private AndroidNotificationService androidService;
    
    @EJB
    private IOSNotificationService iosService;
    
    @Asynchronous
    public void enviaNotificacoes(FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push) throws Exception {
        enviaAndroid(filtro.clone(), push.clone());
        enviaIOS(filtro.clone(), push.clone());
    }

    private void enviaAndroid(FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push){
        filtro.setTipo(TipoDispositivo.ANDROID);
        
        BuscaPaginadaDTO<Object[]> dispositivos;

        do{
            dispositivos = daoService.findWith(new FiltroDispositivoNotificacao(filtro));
            androidService.pushNotifications(filtro.getIgreja(), push, dispositivos.getResultados());
            filtro.proxima();
        }while(dispositivos.isHasProxima());
    }

    private void enviaIOS(FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push){
        filtro.setTipo(TipoDispositivo.IPHONE);
        
        BuscaPaginadaDTO<Object[]> dispositivos;

        do{
            dispositivos = daoService.findWith(new FiltroDispositivoNotificacao(filtro));
            iosService.pushNotifications(filtro.getIgreja(), push, dispositivos.getResultados());
            filtro.proxima();
        }while(dispositivos.isHasProxima());
    }
}
