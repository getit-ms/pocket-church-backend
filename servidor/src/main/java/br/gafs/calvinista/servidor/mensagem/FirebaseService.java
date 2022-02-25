package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.Igreja;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.ejb.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 11/06/2017.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class FirebaseService {

    private static final Logger LOGGER = Logger.getLogger(FirebaseService.class.getName());

    @EJB
    private FirebaseTokenProvider tokenProvider;

    private static final ObjectMapper om = new ObjectMapper();

    @Asynchronous
    public void pushNotifications(Igreja igreja, MensagemPushDTO push, List<Destination> destinations) {

        for (Destination destination : destinations) {
            String token = tokenProvider.getToken(igreja.getChave(), destination.getVersion());

            doSendNotification(new Requisicao(
                    destination.getTo(),
                    push.getTitle(),
                    push.getMessage(),
                    destination.getBadge(),
                    push.getCustomData()
            ), token);
        }
    }

    private boolean doSendNotification(Requisicao requisicao, String token) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL("https://fcm.googleapis.com/fcm/send").openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", "key=" + token);
            urlConnection.setDoOutput(true);

            urlConnection.connect();

            LOGGER.log(Level.WARNING, "Push Firebase: '" + requisicao.get("notification") + "' para " + requisicao.get("to"));

            requisicao.json(new BufferedOutputStream(urlConnection.getOutputStream()));

            InputStream is = new BufferedInputStream(urlConnection.getInputStream());

            Map<String, Object> response = om.readValue(is, Map.class);

            LOGGER.log(Level.WARNING, "Response Push Firebase: '" + response);

            urlConnection.disconnect();

            return "0".equals(response.get("failure").toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro no envio de push " + requisicao.json(), e);

            return false;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Destination {
        private final String to;
        private final String version;
        private final Long badge;
    }

    @Getter
    @AllArgsConstructor
    private static class Requisicao extends ObjectBuilder {

        public Requisicao(String to, String title, String body, Long badge, Map<String, Object> args) {
            prop("to", to);
            prop("notification", obj()
                    .prop("title", title)
                    .prop("body", body)
                    .prop("badge", badge)
            );
            prop("data", obj()
                    .prop("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .prop("badgeCount", badge)
                    .propAll(args)
            );
            prop("content_avaiable", "1");
        }

    }

    static class ObjectBuilder extends HashMap<String, Object> {

        static ObjectBuilder obj() {
            return new ObjectBuilder();
        }

        public ObjectBuilder prop(String chave, Object valor) {
            put(chave, valor);
            return this;
        }

        public ObjectBuilder propAll(Map<String, ?> props) {
            putAll(props);
            return this;
        }

        public void json(OutputStream os) throws IOException {
            om.writeValue(os, this);
        }

        public String json() {
            try {
                return om.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                LOGGER.log(Level.SEVERE, "Erro na seriaizaçao", e);
                return null;
            }
        }

    }

}
