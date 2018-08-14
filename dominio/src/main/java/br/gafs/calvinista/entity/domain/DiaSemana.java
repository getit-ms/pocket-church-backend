/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@RequiredArgsConstructor
public enum DiaSemana {
    DOMINGO(Calendar.SUNDAY, 1),
    SEGUNDA(Calendar.MONDAY, 2),
    TERCA(Calendar.TUESDAY, 4),
    QUARTA(Calendar.WEDNESDAY, 8),
    QUINTA(Calendar.THURSDAY, 16),
    SEXTA(Calendar.FRIDAY, 32),
    SABADO(Calendar.SATURDAY, 64);
    
    private static final int MASK = 127;
    
    private final int dia;
    @Getter
    private final Integer value;
    
    public int dia(){
        return dia;
    }
    
    public static DiaSemana get(int cal){
        for (DiaSemana ds : values()){
            if (ds.dia() == cal){
                return ds;
            }
        }
        return null;
    }
    
    public boolean is(int value){
        return (value & this.value) != 0;
    }
    
    public int set(int into){
        return into | value;
    }
    
    public int unset(int into){
        return (value ^ MASK) & into;
    }
    
    public static int valueOf(List<DiaSemana> diasSemana){
        int value = 0;
        for (DiaSemana dia : diasSemana){
            value = dia.set(value);
        }
        return value;
    }
    
    public static List<DiaSemana> values(int value){
        List<DiaSemana> dias = new ArrayList<DiaSemana>();
        for (DiaSemana dia : values()){
            if (dia.is(value)){
                dias.add(dia);
            }
        }
        return dias;
    }
}
