/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisualizacaoNotificacaoId implements Serializable {
    private String chaveDispositivo;
    private Long idNotificacao;
    private String chaveIgreja;
    
    public VisualizacaoNotificacaoId(String dispositivo, RegistroIgrejaId notificacao){
        this(dispositivo, notificacao.getId(), notificacao.getChaveIgreja());
    }
}
