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
public class RespostaVotacaoId implements Serializable {
    private Long id;
    private Long idVotacao;
    private String chaveIgreja;

    public RespostaVotacaoId(Long id, RegistroIgrejaId votacao) {
        this(id, votacao.getId(), votacao.getChaveIgreja());
    }
}
