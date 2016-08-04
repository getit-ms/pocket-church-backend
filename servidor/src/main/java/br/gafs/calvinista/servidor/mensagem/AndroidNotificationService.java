/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Stateless
public class AndroidNotificationService implements Serializable {
    
    @EJB
    private ParametroService paramService;
    
    private ObjectMapper om = new ObjectMapper();
    
    public void pushNotifications(Igreja igreja, MensagemPushDTO notification, String... tos) {
        pushNotifications(igreja, notification, Arrays.asList(tos));
    }
    
    public void pushNotifications(Igreja igreja, MensagemPushDTO notification, List<String> tos) {
        try {
            String chave = paramService.get(igreja.getChave(), TipoParametro.PUSH_ANDROID_KEY);
            PushAndroidDTO push = new PushAndroidDTO(notification);
            for (String to : tos) {
                doSendNotification(push.cloneTo(to), chave);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
    
    private void doSendNotification(PushAndroidDTO notification, String chave) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://gcm-http.googleapis.com/gcm/send").openConnection();
        
        urlConnection.setRequestMethod("POST");
        urlConnection.addRequestProperty("Content-Type", "application/json");
        urlConnection.addRequestProperty("Authorization", "key="+chave);
        urlConnection.setDoOutput(true);
        
        urlConnection.connect();
        
        om.writeValue(urlConnection.getOutputStream(), notification);
        
        System.out.println(">> " + urlConnection.getResponseCode());
        System.out.println(">> " + om.readValue(urlConnection.getInputStream(), Map.class));
        
        urlConnection.disconnect();
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
    @AllArgsConstructor
    public class NotificationDTO implements DTO {
        private String message;
        private String title;
        private String icon;
    }
}

