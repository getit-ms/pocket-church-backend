/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"votacao", "membro"})
public class VotoId implements Serializable {
    private Long idVotacao;
    private Long idMembro;
    private String chaveIgreja;
    
    public VotoId(RegistroIgrejaId votacao, RegistroIgrejaId membro){
        this(votacao.getId(), membro.getId(), membro.getChaveIgreja());
    }
}
