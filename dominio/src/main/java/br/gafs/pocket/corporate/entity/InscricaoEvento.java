/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusInscricaoEvento;
import br.gafs.pocket.corporate.view.View;
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
@ToString(of = {"colaborador", "evento"})
@Table(name = "tb_inscricao_evento")
@EqualsAndHashCode(of = {"colaborador", "evento"})
@NamedQueries({
    @NamedQuery(name = "InscricaoEvento.quantidadeInscricoesEvento", query = "select count(ie.id) from InscricaoEvento ie where ie.evento.id = :idEvento and ie.status in :status"),
    @NamedQuery(name = "InscricaoEvento.findByReferencia", query = "select ie from InscricaoEvento ie where ie.referenciaCheckout = :referencia"),
    @NamedQuery(name = "InscricaoEvento.findReferenciasByStatusAndEmpresa", query = "select ie.referenciaCheckout from InscricaoEvento ie where ie.colaborador.empresa.chave = :empresa and ie.status = :status group by ie.referenciaCheckout"),
    @NamedQuery(name = "InscricaoEvento.findMaxDataByEvento", query = "select max(ie.data) from InscricaoEvento ie where ie.evento.id = :evento and ie.evento.chaveEmpresa = :empresa"),
    @NamedQuery(name = "InscricaoEvento.findAtivosByEmpresa", query = "select ie from InscricaoEvento ie where ie.evento.tipo = :tipo and ie.chaveEmpresa = :chaveEmpresa and ie.evento.dataHoraTermino >= CURRENT_TIMESTAMP and ie.evento.status = :statusEvento and ie.status = :statusInscricao order by ie.data desc"),
    @NamedQuery(name = "InscricaoEvento.findInscricoesColaborador", query = "select ie from InscricaoEvento ie where ie.evento.tipo = :tipo and ie.chaveEmpresa = :empresa and lower(ie.emailInscrito) = :email and ie.evento.status = :statusEvento and ie.status in :statusInscricao")
})
public class InscricaoEvento implements IEntity {
    @Id
    @Column(name = "id_inscricao")
    @SequenceGenerator(name = "seq_inscricao", sequenceName = "seq_inscricao")
    @GeneratedValue(generator = "seq_inscricao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
    })
    private Colaborador colaborador;
    
    @Id
    @JsonIgnore
    @Column(name = "id_evento", insertable = false, updatable = false)
    private Long idEvento;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "id_evento", referencedColumnName = "id_evento"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
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
    @Column(name = "referencia_checkout")
    private String referenciaCheckout;
    
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

    public InscricaoEvento(Evento evento) {
        this.evento = evento;
        this.valor = evento.getValor();
        if (!evento.isComPagamento()){
            confirmada();
        }
    }
    
    public void confirmada(){
        status = StatusInscricaoEvento.CONFIRMADA;
    }
    
    public void cancelada(){
        status = StatusInscricaoEvento.CANCELADA;
    }
    
    public boolean isConfirmada(){
        return StatusInscricaoEvento.CONFIRMADA.equals(status);
    }
    
    public boolean isPendente(){
        return StatusInscricaoEvento.PENDENTE.equals(status);
    }
    
}