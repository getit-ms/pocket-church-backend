package br.gafs.calvinista.servidor.rest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gabriel on 25/11/2018.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class EasyRESTRequest {
    private final EasyRESTClient client;
    private final StreamConsumer consumer;
    private final StreamProvider provider;

    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final List<String> queryParams = new ArrayList<>();

    public EasyRESTRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public EasyRESTRequest queryParam(String name, String value) {
        try {
            queryParams.add(name + "=" + URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Erro ao adicionar queryParam " + name + "=" + value, e);
        }

        return this;
    }

    protected String getQueryParams() {
        if (queryParams.isEmpty()) {
            return "";
        }

        StringBuilder qp = new StringBuilder("?");
        for (String p : queryParams) {
            qp.append("&").append(p);
        }

        return qp.toString();
    }

    public <T> EasyRESTREsponse<T> get(Class<T> responseType) {
        return connect("GET", null, responseType);
    }

    public <T> EasyRESTREsponse<T> post(Object payload, Class<T> responseType) {
        return connect("POST", payload, responseType);
    }

    public <T> EasyRESTREsponse<T> put(Object payload, Class<T> responseType) {
        return connect("PUT", payload, responseType);
    }

    public <T> EasyRESTREsponse<T> delete(Class<T> responseType) {
        return connect("DELETE", null, responseType);
    }

    protected <T> EasyRESTREsponse<T> connect(String method, Object payload, Class<T> responseType) {
        try{
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(
                    client.getBasePath() + "/" + getPath() + getQueryParams()).openConnection();

            if (!getHeaders().containsKey("Content-Type")) {
                urlConnection.addRequestProperty("Content-Type", provider.getContentType());
            }

            for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
                urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }

            urlConnection.setRequestMethod(method);

            urlConnection.setDoOutput(payload != null);

            urlConnection.connect();

            try {
                if (payload != null) {
                    provider.write(payload, urlConnection.getOutputStream());
                }

                Object response = consumer.read(
                        urlConnection.getResponseCode() >= 200 &&
                                urlConnection.getResponseCode() < 300 &&
                                responseType != null ? responseType : Map.class,
                        urlConnection.getInputStream());

                return new EasyRESTREsponse(urlConnection.getResponseCode(), response);
            } finally {
                urlConnection.disconnect();
            }
        }catch(Exception e){
            throw new RuntimeException("Erro ao fazer a requisição " + method + " " + getPath(), e);
        }
    }

}
