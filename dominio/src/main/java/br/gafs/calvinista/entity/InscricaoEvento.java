/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusInscricaoEvento;
import br.gafs.calvinista.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Entity
@Getter
@NoArgsConstructor
@IdClass(InscricaoEventoId.class)
@ToString(of = {"membro", "evento"})
@Table(name = "tb_inscricao_evento")
@EqualsAndHashCode(of = {"membro", "evento"})
@NamedQueries({
    @NamedQuery(name = "InscricaoEvento.quantidadeInscricoesEvento", query = "select count(ie.id) from InscricaoEvento ie where ie.evento.id = :idEvento and ie.status in :status"),
    @NamedQuery(name = "InscricaoEvento.deleteByEvento", query = "delete from InscricaoEvento ie where ie.evento.id = :idEvento")
})
public class InscricaoEvento implements IEntity {
    @Id
    @Column(name = "id_inscricao")
    @SequenceGenerator(name = "seq_inscricao", sequenceName = "seq_inscricao")
    @GeneratedValue(generator = "seq_inscricao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private Membro membro;
    
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "id_evento", referencedColumnName = "id_evento"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Evento evento;
    
    @Setter
    @NotEmpty
    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome_inscrito", length = 255, nullable = false)
    private String nomeInscrito;
    
    @Setter
    @Email
    @NotEmpty
    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "email_inscrito", length = 150, nullable = false)
    private String emailInscrito;
    
    @Setter
    @NotEmpty
    @Length(max = 50)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "telefone_inscrito", length = 50, nullable = false)
    private String telefoneInscrito;
    
    @Setter
    @Column(name = "chave_checkout")
    private String chaveCheckout;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_inscricao", nullable = false)
    private Date data = DateUtil.getDataAtual();
    
    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusInscricaoEvento status = StatusInscricaoEvento.PENDENTE;

    public InscricaoEvento(Membro membro, Evento evento) {
        this.membro = membro;
        this.evento = evento;
        this.valor = evento.getValor();
        if (!evento.isComPagamento()){
            confirmada();
        }
    }
    
    public void confirmada(){
        status = StatusInscricaoEvento.CONFIRMADA;
    }
    
    public boolean isConfirmada(){
        return StatusInscricaoEvento.CONFIRMADA.equals(status);
    }
    
    public boolean isPendente(){
        return StatusInscricaoEvento.PENDENTE.equals(status);
    }
    
}
