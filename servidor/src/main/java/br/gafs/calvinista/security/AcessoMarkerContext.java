package br.gafs.calvinista.security;

import br.gafs.calvinista.entity.domain.Funcionalidade;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AcessoMarkerContext {
    private static final Map<Long, Funcionalidade> funcs = new HashMap<Long, Funcionalidade>();

    public synchronized static void clear() {
        funcs.remove(Thread.currentThread().getId());
    }

    public synchronized static void funcionalidade(Funcionalidade func) {
        funcs.put(Thread.currentThread().getId(), func);
    }

    public synchronized static Funcionalidade funcionalidade() {
        return funcs.get(Thread.currentThread().getId());
    }
}
