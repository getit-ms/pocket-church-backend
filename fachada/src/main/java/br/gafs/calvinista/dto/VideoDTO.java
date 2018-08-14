/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@RequiredArgsConstructor
public class VideoDTO implements Serializable {
    private final String id;
    private final String titulo;
    private final String descricao;
    private String thumbnail;
    private final Date publicacao;
    private Date agendamento;
    private boolean aoVivo;

    public boolean isAgendado() {
        return agendamento != null;
    }
    
}
