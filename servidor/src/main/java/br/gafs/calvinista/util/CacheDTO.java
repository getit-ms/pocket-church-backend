package br.gafs.calvinista.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Gabriel on 23/07/2018.
 */
@Getter
@RequiredArgsConstructor
public class CacheDTO<T> {
    private final T dados;
    private final long timeout;

    public boolean isExpirado() {
        return System.currentTimeMillis() > timeout;
    }
}
