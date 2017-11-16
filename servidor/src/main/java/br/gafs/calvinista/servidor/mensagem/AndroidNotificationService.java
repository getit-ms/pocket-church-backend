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
import br.gafs.dto.DTO;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import lombok.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;

/**
 *
 * @author Gabriel
 */
@Stateless
public class AndroidNotificationService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(AndroidNotificationService.class.getName());

    @EJB
    private ParametroService paramService;

    private ObjectMapper om = new ObjectMapper();

    @Asynchronous
    public void pushNotifications(Igreja igreja, MensagemPushDTO notification, List<Destination> destinations) {

        String chave = paramService.get(igreja.getChave(), TipoParametro.PUSH_ANDROID_KEY);

        PushAndroidDTO push = new PushAndroidDTO(notification);
        for (Destination destination : destinations) {
            doSendNotification(push.cloneTo(destination.getTo(), destination.getBadge()), chave);
        }
    }

    private boolean doSendNotification(PushAndroidDTO notification, String chave) {
        try{
            HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://gcm-http.googleapis.com/gcm/send").openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", "key="+chave);
            urlConnection.setDoOutput(true);

            urlConnection.connect();

            LOGGER.log(Level.WARNING, "Push Android: '" + notification.getData().getMessage() + "' para " + notification.getTo());

            OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
            om.writeValue(os, notification);

            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            System.out.println(">> " + urlConnection.getResponseCode());

            Map<String, Object> response = om.readValue(is, Map.class);
            LOGGER.log(Level.WARNING, "Response Push Android: '" + response);

            urlConnection.disconnect();

            return "0".equals(response.get("failure").toString());
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Erro ao enviar push para " + notification.getTo(), e);
            return false;
        }
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public class PushAndroidDTO implements DTO, Cloneable {
        private String to;
        private final NotificationDTO data;

        public PushAndroidDTO(MensagemPushDTO notification) {
            this(new NotificationDTO(
                    notification.getMessage(),
                    notification.getTitle(),
                    notification.getIcon(),
                    notification.getCustomData()));
        }

        public PushAndroidDTO cloneTo(String to, Long badge) {
            try {
                PushAndroidDTO clone = (PushAndroidDTO) this.clone();
                clone.setTo(to);
                clone.data.put("badge", badge);
                clone.data.put("badgeCount", badge);
                return clone;
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(PushAndroidDTO.class.getName()).log(Level.SEVERE, null, ex);
                return new PushAndroidDTO(to, data);
            }

        }
    }

    @Data
    @NoArgsConstructor
    public class NotificationDTO extends HashMap<String, Object> {

        private NotificationDTO(String message, String title, String icon, Map<String, Object> customData) {
            if (!StringUtil.isEmpty(title)){
                put("title", title);
            }
            if (!StringUtil.isEmpty(message)){
                put("body", message);
            }
            if (!StringUtil.isEmpty(icon)){
                put("icon", icon);
            }
            put("style", "inbox");
            put("content-available", 1);
            put("summaryText", "%n% notificações");
            putAll(customData);
        }

        private String getMessage() {
            return (String) get("message");
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Destination {
        private final String to;
        private final Long badge;
    }
}

