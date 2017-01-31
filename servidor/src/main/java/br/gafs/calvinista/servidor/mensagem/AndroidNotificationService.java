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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    public List<String> pushNotifications(Igreja igreja, MensagemPushDTO notification, Object[]... tos) {
        return pushNotifications(igreja, notification, Arrays.asList(tos));
    }
    
    public List<String> pushNotifications(Igreja igreja, MensagemPushDTO notification, List<Object[]> tos) {
        List<String> failures = new ArrayList<String>();
        try {
            String chave = paramService.get(igreja.getChave(), TipoParametro.PUSH_ANDROID_KEY);
            PushAndroidDTO push = new PushAndroidDTO(notification);
            for (Object[] to : tos) {
                if (!doSendNotification(push.cloneTo((String) to[0], (Long) to[1]), chave)){
                    failures.add((String) to[0]);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            
        }
        return failures;
    }
    
    private boolean doSendNotification(PushAndroidDTO notification, String chave) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://gcm-http.googleapis.com/gcm/send").openConnection();
        
        urlConnection.setRequestMethod("POST");
        urlConnection.addRequestProperty("Content-Type", "application/json");
        urlConnection.addRequestProperty("Authorization", "key="+chave);
        urlConnection.setDoOutput(true);
        
        urlConnection.connect();
        
        LOGGER.log(Level.WARNING, "Push Android: '" + notification.getData().getMessage() + "' para " + notification.getTo());
        
        om.writeValue(urlConnection.getOutputStream(), notification);
        
        System.out.println(">> " + urlConnection.getResponseCode());
        
        Map<String, Object> response = om.readValue(urlConnection.getInputStream(), Map.class);
        LOGGER.log(Level.WARNING, "Response Push Android: '" + response);
        
        urlConnection.disconnect();

        return "0".equals(response.get("failure").toString());
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
                put("message", message);
            }
            if (!StringUtil.isEmpty(icon)){
                put("icon", icon);
            }
            put("style", "inbox");
            put("content-available", 1);
            putAll(customData);
        }

        private String getMessage() {
            return (String) get("message");
        }
    }
}

