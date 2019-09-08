package br.gafs.pocket.corporate.servidor.rest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Gabriel on 25/11/2018.
 */
public interface StreamConsumer {
    <T> T read(Class<T> type, InputStream is) throws IOException;
}
