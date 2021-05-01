package br.gafs.calvinista.servidor.rest;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Gabriel on 25/11/2018.
 */
public interface StreamProvider {
    void write(Object o, OutputStream os) throws IOException;
    String getContentType();
}
