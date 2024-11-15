/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusVotacao;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

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
@Table(name = "tb_votacao")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
public class Votacao implements IEntity {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_votacao")
    @SequenceGenerator(name = "seq_votacao", sequenceName = "seq_votacao")
    @GeneratedValue(generator = "seq_votacao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 150)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
    
    @NotEmpty
    @Length(max = 500)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao", length = 500, nullable = false)
    private String descricao;
    
    @NotNull
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_inicio", nullable = false)
    private Date dataInicio;
    
    @JsonView(Resumido.class)
    @Column(name = "data_termino")
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    private Date dataTermino;
    
    @JsonView(Detalhado.class)
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusVotacao status = StatusVotacao.PUBLICADO;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;
    
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "votacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Questao> questoes = new ArrayList<Questao>();

    @Transient
    @JsonView(View.Resumido.class)
    private boolean respondido;

    public Votacao(Igreja igreja) {
        this.igreja = igreja;
    }

    public List<Questao> getQuestoes(){
        Collections.sort(questoes);
        return questoes;
    }
    
    @JsonView(Detalhado.class)
    public StatusVotacao getStatusEfetivo(){
        if (dataInicio == null) {
            return StatusVotacao.EM_EDICAO;
        }

        if (DateUtil.getDataAtual().before(dataInicio)){
            return StatusVotacao.AGENDADO;
        }

        if (dataTermino != null && DateUtil.getDataAtual().after(dataTermino)){
            return StatusVotacao.ENCERRADO;
        }

        return StatusVotacao.PUBLICADO;
    }
    
    @JsonView(Detalhado.class)
    public boolean isAgendado(){
        return StatusVotacao.AGENDADO.equals(getStatusEfetivo());
    }
    
    @JsonView(Detalhado.class)
    public boolean isPublicado(){
        return StatusVotacao.PUBLICADO.equals(getStatusEfetivo());
    }
    
    @JsonView(Resumido.class)
    public boolean isEncerrado(){
        return StatusVotacao.ENCERRADO.equals(getStatusEfetivo());
    }

}
