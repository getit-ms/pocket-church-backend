package br.gafs.calvinista.servidor.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.text.MessageFormat;

/**
 * Created by Gabriel on 25/11/2018.
 */
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
public class EasyRESTClient {
    private static ObjectMapper om = new ObjectMapper();

    static {
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static final StreamProvider PROVIDER_APPLICATOIN_JSON = new StreamProvider() {
        @Override
        public void write(Object o, OutputStream os) throws IOException {
            om.writeValue(new BufferedOutputStream(os), o);
        }

        @Override
        public String getContentType() {
            return "application/json";
        }
    };

    public static final StreamConsumer CONSUMER_APPLICATION_JSON = new StreamConsumer() {
        @Override
        public <T> T read(Class<T> type, InputStream is) throws IOException {
            return om.readValue(new BufferedInputStream(is), type);
        }
    };

    private final String basePath;

    public EasyRESTRequest requestJSON(String pathFormat, Object... pathArgs) {
        return request(pathFormat, pathArgs, CONSUMER_APPLICATION_JSON, PROVIDER_APPLICATOIN_JSON);
    }

    public EasyRESTRequest request(String pathFormat,
                                   Object[] pathArgs,
                                   StreamConsumer consumer,
                                   StreamProvider provider) {
        return new EasyRESTRequest(this, consumer, provider,
                MessageFormat.format(pathFormat, pathArgs));
    }

}
