/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusVotacao;
import br.gafs.calvinista.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

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
    @Column(name = "id_votacao")
    @SequenceGenerator(name = "seq_votacao", sequenceName = "seq_votacao")
    @GeneratedValue(generator = "seq_votacao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
    
    @NotEmpty
    @Length(max = 500)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao", length = 500, nullable = false)
    private String descricao;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_inicio", nullable = false)
    private Date dataInicio;
    
    @Column(name = "data_termino")
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    private Date dataTermino;
    
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusVotacao status = StatusVotacao.EM_EDICAO;
    
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;
    
    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "votacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Questao> questoes = new ArrayList<Questao>();

    public Votacao(Igreja igreja) {
        this.igreja = igreja;
    }

    public List<Questao> getQuestoes(){
        Collections.sort(questoes);
        return questoes;
    }
    
    public StatusVotacao getStatusEfetivo(){
        if (StatusVotacao.PUBLICADO.equals(status)){
            if (DateUtil.getDataAtual().before(dataInicio)){
                if (dataTermino != null && DateUtil.getDataAtual().after(dataTermino)){
                    return StatusVotacao.ENCERRADO;
                }
                return StatusVotacao.AGENDADO;
            }
        }
        return status;
    }
    
    public boolean isEmEdicao(){
        return StatusVotacao.EM_EDICAO.equals(getStatusEfetivo());
    }
    
    public boolean isAgendado(){
        return StatusVotacao.AGENDADO.equals(getStatusEfetivo());
    }
    
    public boolean isPublicado(){
        return StatusVotacao.PUBLICADO.equals(getStatusEfetivo());
    }
    
    public boolean isEncerrado(){
        return StatusVotacao.ENCERRADO.equals(getStatusEfetivo());
    }
    
    public void publica(){
        if (isEmEdicao()){
            status = StatusVotacao.PUBLICADO;
            if (DateUtil.getDataAtual().before(dataInicio)){
                status = StatusVotacao.AGENDADO;
            }else{
            }
        }
    }
    
    public void encerra(){
        if (isPublicado()){
            status = StatusVotacao.ENCERRADO;
            dataTermino = DateUtil.getDataAtual();
        }
    }
    
    public void cancelaAgendamento(){
        if (isAgendado()){
            status = StatusVotacao.EM_EDICAO;
        }
    }

    public void setDataInicio(Date dataInicio) {
        if (isEmEdicao()){
            this.dataInicio = dataInicio;
        }
    }

    public void setDataTermino(Date dataTermino) {
        if (isEmEdicao()){
            this.dataTermino = dataTermino;
        }
    }

    public void setDescricao(String descricao) {
        if (isEmEdicao()){
            this.descricao = descricao;
        }
    }

    public void setNome(String nome) {
        if (isEmEdicao()){
            this.nome = nome;
        }
    }
    
}
