/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

import br.gafs.util.date.DateUtil;
import java.util.Date;
import lombok.Getter;

/**
 *
 * @author Gabriel
 */
@Getter
public enum HorasEnvioNotificacao {
    _08_00("08:00"),
    _14_00("14:00"),
    _20_00("20:00");
    
    private final Date hora;
    private final Integer horaInt;
    
    public Integer getHoraInt(){
        return horaInt;
    }

    private HorasEnvioNotificacao(String hora) {
        this.hora = DateUtil.parseData(hora, "HH:mm");
        this.horaInt = Integer.parseInt(DateUtil.formataData(this.hora, "HH"));
    }
    
}
