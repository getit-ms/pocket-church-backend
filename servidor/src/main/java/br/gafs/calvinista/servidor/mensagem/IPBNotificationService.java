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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.ejb.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class IPBNotificationService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(IPBNotificationService.class.getName());

    @EJB
    private ParametroService paramService;

    private ObjectMapper om = new ObjectMapper();

    @Asynchronous
    public void pushNotifications(Igreja igreja, MensagemPushDTO notification, List<Destination> destinations) {

        String chave = paramService.get(igreja.getChave(), TipoParametro.IPB_PUSH_TOKEN);

        String titulo = igreja.getNomeAplicativo() +
                (StringUtil.isEmpty(notification.getTitle()) ? "" : (" - " + notification.getTitle()));

        PushIPBDTO push = new PushIPBDTO(titulo, notification.getMessage());
        for (Destination destination : destinations) {
            doSendNotification(push.cloneTo(destination.getTo()), chave);
        }
    }

    private boolean doSendNotification(PushIPBDTO notification, String chave) {
        try{
            HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://localhost:8080/ipb/app/notificacao/push").openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", "App "+chave);
            urlConnection.setDoOutput(true);

            urlConnection.connect();

            LOGGER.log(Level.WARNING, "Push IPB: '" + notification.getMessage() + "' para " + notification.getTo());

            OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
            om.writeValue(os, notification);

            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            System.out.println(">> " + urlConnection.getResponseCode());

            Map<String, Object> response = om.readValue(is, Map.class);
            LOGGER.log(Level.WARNING, "Response Push IPB: '" + response);

            urlConnection.disconnect();

            return true;
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Erro ao enviar push para " + notification.getTo(), e);
            return false;
        }
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public class PushIPBDTO implements DTO, Cloneable {
        private String to;
        private final String title;
        private final String message;

        public PushIPBDTO cloneTo(String to) {
            return new PushIPBDTO(to, title, message);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Destination {
        private final String to;
        private final Long badge;
    }
}

