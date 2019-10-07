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
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class VideoDTO implements Serializable {
    private final String id;
    private final String titulo;
    private final String descricao;
    private String thumbnail;
    private final Date publicacao;
    private Date agendamento;
    private boolean aoVivo;
    private String streamUrl;

    public boolean isAgendado() {
        return agendamento != null;
    }
    
}
