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
public class InscricaoEventoId implements Serializable{
    private Long id;
    private Long idEvento;
    private String chaveIgreja;
    
    public InscricaoEventoId(Long id, RegistroIgrejaId evento){
        this(id, evento.getId(), evento.getChaveIgreja());
    }
}
