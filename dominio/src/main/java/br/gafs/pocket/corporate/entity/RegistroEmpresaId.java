/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import lombok.*;

import java.io.Serializable;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"chaveEmpresa", "id"})
@EqualsAndHashCode(of = {"chaveEmpresa", "id"})
public class RegistroEmpresaId implements Serializable {
    private String chaveEmpresa;
    private Long id;
}
