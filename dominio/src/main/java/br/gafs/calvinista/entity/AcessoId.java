/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcessoId implements Serializable {
    private Long idMembro;
    private String chaveIgreja;

    public AcessoId(RegistroIgrejaId membro) {
        this(membro.getId(), membro.getChaveIgreja());
    }
}
