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
public class AcessoId implements Serializable {
    private Long idColaborador;
    private String chaveEmpresa;
    
    public AcessoId(RegistroEmpresaId colaborador){
        this(colaborador.getId(), colaborador.getChaveEmpresa());
    }
}
