/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "igreja"})
@EqualsAndHashCode(of = {"id", "igreja"})
public class AgendamentoAtendimentoId implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    @Id
    @Column(name = "chave_igreja")
    private String igreja;
}
