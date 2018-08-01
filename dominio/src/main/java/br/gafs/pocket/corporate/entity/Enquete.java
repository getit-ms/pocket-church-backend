/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusEnquete;
import br.gafs.pocket.corporate.view.View;
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
@Table(name = "tb_enquete")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
public class Enquete implements IEntity {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_enquete")
    @SequenceGenerator(name = "seq_enquete", sequenceName = "seq_enquete")
    @GeneratedValue(generator = "seq_enquete", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 150)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
    
    @NotEmpty
    @Length(max = 500)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao", length = 500, nullable = false)
    private String descricao;
    
    @NotNull
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_inicio", nullable = false)
    private Date dataInicio;
    
    @JsonView(View.Resumido.class)
    @Column(name = "data_termino")
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    private Date dataTermino;
    
    @JsonView(View.Detalhado.class)
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusEnquete status = StatusEnquete.PUBLICADO;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;
    
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "enquete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Questao> questoes = new ArrayList<Questao>();

    @Transient
    @JsonView(View.Resumido.class)
    private boolean respondido;

    public Enquete(Empresa empresa) {
        this.empresa = empresa;
    }

    public List<Questao> getQuestoes(){
        Collections.sort(questoes);
        return questoes;
    }
    
    @JsonView(View.Detalhado.class)
    public StatusEnquete getStatusEfetivo(){
        if (dataInicio == null) {
            return StatusEnquete.EM_EDICAO;
        }

        if (DateUtil.getDataAtual().before(dataInicio)){
            return StatusEnquete.AGENDADO;
        }

        if (dataTermino != null && DateUtil.getDataAtual().after(dataTermino)){
            return StatusEnquete.ENCERRADO;
        }

        return StatusEnquete.PUBLICADO;
    }
    
    @JsonView(View.Detalhado.class)
    public boolean isAgendado(){
        return StatusEnquete.AGENDADO.equals(getStatusEfetivo());
    }
    
    @JsonView(View.Detalhado.class)
    public boolean isPublicado(){
        return StatusEnquete.PUBLICADO.equals(getStatusEfetivo());
    }
    
    @JsonView(View.Resumido.class)
    public boolean isEncerrado(){
        return StatusEnquete.ENCERRADO.equals(getStatusEfetivo());
    }

}
