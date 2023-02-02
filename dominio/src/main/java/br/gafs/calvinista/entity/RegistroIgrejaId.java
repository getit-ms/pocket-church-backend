/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"chaveIgreja", "id"})
@EqualsAndHashCode(of = {"chaveIgreja", "id"})
public class RegistroIgrejaId implements Serializable {
    private String chaveIgreja;
    private Long id;
}
