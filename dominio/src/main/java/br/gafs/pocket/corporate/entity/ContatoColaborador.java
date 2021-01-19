/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusContatoColaborador;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@Table(name = "tb_contato_colaborador")
public class ContatoColaborador implements IEntity {
    @Id
    @Column(name = "id_contato_colaborador")
    @SequenceGenerator(sequenceName = "seq_contato_colaborador", name = "seq_contato_colaborador")
    @GeneratedValue(generator = "seq_contato_colaborador", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_solicitacao", nullable = false)
    private Date dataSolicitacao = DateUtil.getDataAtual();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_atendimento")
    private Date dataAtendimento;
    
    @Setter
    @NotEmpty
    @Length(max = 150)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
    
    @Email
    @Setter
    @NotEmpty
    @Length(max = 150)
    @Column(name = "email", length = 150, nullable = false)
    private String email;
    
    @Setter
    @NotEmpty
    @Length(max = 500)
    @Column(name = "mensagem", length = 500, nullable = false)
    private String mensagem;
    
    @NotNull
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusContatoColaborador status = StatusContatoColaborador.PENDENTE;
    
    @Setter
    @NotNull
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_solicitante", referencedColumnName = "id_colaborador", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false, insertable = false, updatable = false)
    })
    private Colaborador solicitante;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_atendente", referencedColumnName = "id_colaborador"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false, insertable = false, updatable = false)
    })
    private Colaborador atendente;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    public boolean isAtendido(){
        return StatusContatoColaborador.ATENDIDO.equals(status);
    }
    
    public boolean isPendente(){
        return StatusContatoColaborador.PENDENTE.equals(status);
    }
    
    public void atende(Colaborador colaborador){
        if (isPendente()){
            status = StatusContatoColaborador.ATENDIDO;
            dataAtendimento = DateUtil.getDataAtual();
            atendente = colaborador;
        }
    }
    
    public void setSolicitante(Colaborador solicitante){
        this.solicitante = solicitante;
        this.empresa = solicitante.getEmpresa();
    }
}
