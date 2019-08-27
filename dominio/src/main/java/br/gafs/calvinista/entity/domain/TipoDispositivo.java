/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@RequiredArgsConstructor
public enum TipoDispositivo {
    ANDROID("A"),
    IPHONE("I"),
    PC("P"),
    UNKNOWN("U"),
    IPB("IPB"),
    ANDROID_FIREBASE("AF"),
    IPHONE_FIREBASE("IF");
    
    private final String codigo;
    
    public static TipoDispositivo get(String codigo){
        for (TipoDispositivo tipo : values()){
            if (tipo.getCodigo().equals(codigo)){
                return tipo;
            }
        }
        return null;
    }
}
