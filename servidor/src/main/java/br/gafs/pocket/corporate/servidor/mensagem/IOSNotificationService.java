/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor.mensagem;

import br.gafs.pocket.corporate.dto.MensagemPushDTO;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.util.string.StringUtil;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.PreDestroy;
import javax.ejb.*;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@Singleton
@TransactionAttribute(TransactionAttributeType.NEVER)
public class IOSNotificationService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(IOSNotificationService.class.getName());
    
    @EJB
    private ParametroService paramService;

    private Map<String, ApnsService> services = new HashMap<String, ApnsService>();

    @Asynchronous
    public void pushNotifications(Empresa empresa, MensagemPushDTO notification, List<Destination> destinations) {
        try {
            final ApnsService service = getApnsService(empresa);

            Map<String, String> requests = new HashMap<String, String>();
            for (Destination destination : destinations) {
                prepareRequests(requests, notification, destination.getTo(), destination.getBadge());
            }

            synchronized (service) {
                for (Map.Entry<String, String> entry : requests.entrySet()) {
                    try {
                        service.push(entry.getKey(), entry.getValue());
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Erro o enviar push iOS para " + entry.getKey(), ex);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Falha ao preparar serviço para envio de notificação da empresa " + empresa.getChave(), ex);
        }
    }

    @PreDestroy
    @Schedule(hour = "0", persistent = false)
    public synchronized void finalizaServicos() {
        LOGGER.log(Level.INFO, "Encerrando servicos ativos e push iOS: " + services.size());

        for (ApnsService service : services.values()) {
            synchronized (service) {
                service.stop();
            }
        }

        services.clear();
    }

    private synchronized ApnsService getApnsService(Empresa empresa) {
        if (!services.containsKey(empresa.getChave())) {

            synchronized (services) {
                if (!services.containsKey(empresa.getChave())) {
                    ApnsServiceBuilder serviceBuilder = APNS.newService().withCert(
                            new ByteArrayInputStream((byte[]) paramService.get(empresa.getChave(), TipoParametro.PUSH_IOS_CERTIFICADO)),
                            (String) paramService.get(empresa.getChave(), TipoParametro.PUSH_IOS_PASS));

                    serviceBuilder.withProductionDestination();

                    ApnsService service = serviceBuilder.build();

                    service.start();

                    service.testConnection();

                    services.put(empresa.getChave(), service);
                }
            }
        }

        return services.get(empresa.getChave());
    }
    
    private void prepareRequests(Map<String, String> requests, MensagemPushDTO notification, String to, Long badge) {
        PayloadBuilder builder = APNS.newPayload();
        
        if (!StringUtil.isEmpty(notification.getSound())){
            builder.sound(notification.getSound());
        }
        
        builder.customFields(notification.getCustomData());
        
        LOGGER.log(Level.WARNING, "Push iOS: '" + notification.getMessage() + "' para " + to);

        builder.badge(badge.intValue());

        if (!StringUtil.isEmpty(notification.getMessage())){
            builder.alertBody(notification.getMessage());
        }

        if (!StringUtil.isEmpty(notification.getTitle())){
            builder.alertTitle(notification.getTitle());
        }

        requests.put(to, builder.build());
    }

    @Getter
    @RequiredArgsConstructor
    public static class Destination {
        private final String to;
        private final Long badge;
    }
}

