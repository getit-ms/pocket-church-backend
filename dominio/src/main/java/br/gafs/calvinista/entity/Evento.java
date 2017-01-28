/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_evento")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
public class Evento implements IEntity {
    @Id
    @Column(name = "id_evento")
    @SequenceGenerator(name = "seq_evento", sequenceName = "seq_evento")
    @GeneratedValue(generator = "seq_evento", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 250)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 250, nullable = false)
    private String nome;
    
    @Column(name = "descricao")
    @View.MergeViews(View.Edicao.class)
    private String descricao;
    
    @Setter
    @NotNull
    @View.MergeViews(View.Edicao.class)
    @Column(name = "limite_inscricoes")
    private Integer limiteInscricoes;

    @Setter
    @NotNull
    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    private TipoEvento tipo = TipoEvento.EVENTO;

    @Transient
    private Integer vagasRestantes = 0;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_hora_inicio", nullable = false)
    private Date dataHoraInicio;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_hora_termino", nullable = false)
    private Date dataHoraTermino;
    
    @View.MergeViews(View.Edicao.class)
    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;
    
    @View.MergeViews(View.Edicao.class)
    @Column(name = "exige_pagamento", nullable = false)
    private boolean exigePagamento = true;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_inicio_inscricao", nullable = false)
    private Date dataInicioInscricao;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_fim_inscricao", nullable = false)
    private Date dataTerminoInscricao;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @ManyToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public Evento(Igreja igreja) {
        this.igreja = igreja;
    }
    
    public void setVagasRetantes(Integer vagasRestantes){
        this.vagasRestantes = Math.max(0, vagasRestantes);
    }
    
    public boolean isPublicado(){
        return !DateUtil.getDataAtual().after(dataHoraTermino);
    }
    
    public boolean isInscricoesFuturas(){
        return isPublicado() &&
                DateUtil.getDataAtual().before(dataInicioInscricao);
    }
    
    public boolean isInscricoesPassadas(){
        return isPublicado() &&
                DateUtil.getDataAtual().after(dataTerminoInscricao);
    }
    
    public boolean isInscricoesAbertas(){
        return isPublicado() &&
                !DateUtil.getDataAtual().before(dataInicioInscricao) &&
                !DateUtil.getDataAtual().after(dataTerminoInscricao);
    }
    
    
    public boolean isComPagamento(){
        return valor != null && exigePagamento;
    }
    
}
