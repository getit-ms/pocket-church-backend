/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "id")
@Table(name = "tb_biblia")
@EqualsAndHashCode(of = "id")
public class Biblia implements IEntity {
    @Id
    @Column(name = "id_biblia")
    @SequenceGenerator(name = "seq_biblia", sequenceName = "seq_biblia")
    @GeneratedValue(generator = "seq_biblia", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "descricao", length = 150)
    private String descricao;
}
