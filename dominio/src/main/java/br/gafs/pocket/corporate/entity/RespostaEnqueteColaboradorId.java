/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

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
@EqualsAndHashCode(of = {"enquete", "colaborador"})
public class RespostaEnqueteColaboradorId implements Serializable {
    private Long idEnquete;
    private Long idColaborador;
    private String chaveEmpresa;
    
    public RespostaEnqueteColaboradorId(RegistroEmpresaId enquete, RegistroEmpresaId colaborador){
        this(enquete.getId(), colaborador.getId(), colaborador.getChaveEmpresa());
    }
}
