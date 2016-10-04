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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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
    
    public List<String> pushNotifications(Igreja igreja, MensagemPushDTO notification, String... tos) {
        return pushNotifications(igreja, notification, Arrays.asList(tos));
    }
    
    public List<String> pushNotifications(Igreja igreja, MensagemPushDTO notification, List<String> tos) {
        List<String> failures = new ArrayList<String>(tos);
        try {
            String chave = paramService.get(igreja.getChave(), TipoParametro.PUSH_ANDROID_KEY);
            PushAndroidDTO push = new PushAndroidDTO(notification);
            for (String to : tos) {
                if (doSendNotification(push.cloneTo(to), chave)){
                    failures.remove(to);
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
                    notification.getIcon()));
        }
        
        public PushAndroidDTO cloneTo(String to) {
            try {
                PushAndroidDTO clone = (PushAndroidDTO) this.clone();
                clone.setTo(to);
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

        private NotificationDTO(String message, String title, String icon) {
            put("message", message);
            put("title", title);
            put("icon", icon);
            put("style", "inbox");
            put("content-available", 1);
        }

        private String getMessage() {
            return (String) get("message");
        }
    }
}

