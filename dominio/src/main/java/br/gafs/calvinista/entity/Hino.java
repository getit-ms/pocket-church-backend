/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@Table(name = "tb_hino")
@EqualsAndHashCode(of = {"id", "igreja"})
public class Hino implements IEntity {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_hino")
    @SequenceGenerator(sequenceName = "seq_hino", name = "seq_hino")
    @GeneratedValue(generator = "seq_hino", strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty
    @Length(max = 10)
    @JsonView(Resumido.class)
    @Column(name = "numero")
    @View.MergeViews(View.Edicao.class)
    private String numero;

    @NotEmpty
    @Length(max = 150)
    @Column(name = "assunto")
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    private String assunto;

    @NotEmpty
    @Length(max = 150)
    @Column(name = "autor")
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    private String autor;
    
    @NotEmpty
    @Length(max = 150)
    @JsonView(Resumido.class)
    @Column(name = "nome")
    @View.MergeViews(View.Edicao.class)
    private String nome;
    
    @NotEmpty
    @JsonView(Detalhado.class)
    @Column(name = "texto", columnDefinition = "TEXT")
    @View.MergeViews(View.Edicao.class)
    private String texto;
    
    @JsonView(Detalhado.class)
    @Column(name = "locale")
    private String locale;
    
}
