/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.LivroBiblia;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_versiculo_biblia")
public class VersiculoBiblia implements IEntity {
    @Id
    @Column(name = "id_versiculo_biblia")
    @GeneratedValue(generator = "seq_versiculo_biblia", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_versiculo_biblia", sequenceName = "seq_versiculo_biblia")
    private Long id;
    
    @Column(name = "capitulo")
    private Integer capitulo;
    
    @Column(name = "versiculo")
    private Integer versiculo;
    
    @Column(name = "texto", length = 150)
    private String texto;
}
