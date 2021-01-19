package br.gafs.pocket.corporate.servidor.rest;

import lombok.Getter;

import java.util.Map;

/**
 * Created by Gabriel on 25/11/2018.
 */
@Getter
public class EasyRESTResponse<T> {
    private int status;
    private T body;
    private Map<String, Object> error;

    public EasyRESTResponse(int status, Object response) {
        this.status = status;

        if (isSucesso()) {
            this.body = (T) response;
        } else {
            this.error = (Map) response;
        }
    }

    public boolean isSucesso() {
        return status >= 200 && status < 300;
    }
}

