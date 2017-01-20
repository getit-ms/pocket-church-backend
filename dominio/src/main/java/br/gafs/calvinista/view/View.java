/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Gabriel
 */
public class View {
    public static class AlterarSenha {}
    public static class Edicao {}
    public static class Cadastro {}

    public static interface Resumido {}
    public static interface Detalhado extends Resumido {}
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface MergeViews {
        Class[] value();
    }
}
