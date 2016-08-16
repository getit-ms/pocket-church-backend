/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.StatusBoletim;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_boletim")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
public class Boletim implements ArquivoPDF {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_boletim")
    @SequenceGenerator(name = "seq_boletim", sequenceName = "seq_boletim")
    @GeneratedValue(generator = "seq_boletim", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "titulo")
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    private String titulo;
    
    @Column(name = "data")
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    private Date data;
    
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    private Date dataPublicacao;

    @JsonView(Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusBoletim status = StatusBoletim.PUBLICADO;
    
    @NotNull
    @OneToOne
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
        @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo boletim;
    
    @Setter
    @NotNull
    @OneToOne
    @JsonView(Resumido.class)
    @JoinColumns({
        @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;
    
    @ManyToMany
    @JsonView(Resumido.class)
    @JoinTable(name = "rl_boletim_paginas", joinColumns = {
        @JoinColumn(name = "id_boletim", referencedColumnName = "id_boletim", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    }, inverseJoinColumns = {
        @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private List<Arquivo> paginas = new ArrayList<Arquivo>();
    
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    public Boletim(Igreja igreja) {
        this.igreja = igreja;
    }
    
    public void publica(){
        if (dataPublicacao == null){
            dataPublicacao = DateUtil.getDataAtual();
        }
        status = StatusBoletim.PUBLICADO;
    }
    
    public boolean isPublicado(){
        return StatusBoletim.PUBLICADO.equals(status);
    }
    
    public boolean isEmEdicao(){
        return StatusBoletim.EM_EDICAO.equals(status);
    }
    
    public List<Arquivo> getPaginas(){
        Collections.sort(paginas);
        return paginas;
    }

    @Override
    @JsonIgnore
    public Arquivo getPDF() {
        return boletim;
    }
}
