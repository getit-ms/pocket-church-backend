/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusBoletim;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
public class Boletim implements IEntity {
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
}
