/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

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
public class RespostaEnqueteId implements Serializable {
    private Long id;
    private Long idEnquete;
    private String chaveEmpresa;
    
    public RespostaEnqueteId(Long id, RegistroEmpresaId enquete){
        this(id, enquete.getId(), enquete.getChaveEmpresa());
    }
}
