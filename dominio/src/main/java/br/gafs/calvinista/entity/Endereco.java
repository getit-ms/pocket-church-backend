/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

/**
 * @author Gabriel
 */
@Data
@Entity
@ToString(of = "id")
@Table(name = "tb_endereco")
@EqualsAndHashCode(of = "id")
public class Endereco implements IEntity {
    @Id
    @Column(name = "id_endereco")
    @SequenceGenerator(name = "seq_endereco", sequenceName = "seq_endereco")
    @GeneratedValue(generator = "seq_endereco", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Length(max = 255)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao", length = 255)
    private String descricao;

    @Length(max = 10)
    @Column(name = "cep", length = 10)
    @View.MergeViews(View.Edicao.class)
    private String cep;

    @Length(max = 100)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "cidade", length = 100)
    private String cidade;

    @Length(max = 100)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "estado", length = 100)
    private String estado;
}
