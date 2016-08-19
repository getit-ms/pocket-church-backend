/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.util.string.StringUtil;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Gabriel
 */
@Stateless
public class IOSNotificationService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(IOSNotificationService.class.getName());
    
    @EJB
    private ParametroService paramService;
    
    public void pushNotifications(Igreja igreja, MensagemPushDTO notification, String... to) {
        pushNotifications(igreja, notification, Arrays.asList(to));
    }
    
    public void pushNotifications(Igreja igreja, MensagemPushDTO notification, List<String> tos) {
        try{
            ApnsService service = createApnsService(igreja);
            service.start();

            for (String to : tos) {
                doSendNotification(notification, to, service);
            }

            service.stop();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private ApnsService createApnsService(Igreja igreja) {
        ApnsService service = null;
        ApnsServiceBuilder serviceBuilder = APNS.newService().withCert(
                        new ByteArrayInputStream((byte[]) paramService.get(igreja.getChave(), TipoParametro.PUSH_IOS_CERTIFICADO)), 
                        (String) paramService.get(igreja.getChave(), TipoParametro.PUSH_IOS_PASS));
        serviceBuilder.withProductionDestination();
        service = serviceBuilder.build();
        return service;
    }
    
    private void doSendNotification(MensagemPushDTO notification, String to, ApnsService service) {
        PayloadBuilder builder = APNS.newPayload();
        
        if (notification.getBadge() != null){
            builder.badge(notification.getBadge());
        }
        
        if (!StringUtil.isEmpty(notification.getSound())){
            builder.sound(notification.getSound());
        }
        
        LOGGER.log(Level.WARNING, "Push iOS: '" + notification.getMessage() + "' para " + to);
        
        service.push(to, builder.
                alertBody(notification.getMessage()).
                alertTitle(notification.getTitle()).build());
    }
}

