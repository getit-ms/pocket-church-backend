package br.gafs.pocket.corporate.security;

import br.gafs.pocket.corporate.entity.domain.Funcionalidade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Gabriel
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AcessoMarker {
    Funcionalidade value();
}