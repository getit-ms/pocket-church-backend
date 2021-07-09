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
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
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
        @NamedQuery(name = "InscricaoEvento.findByReferencia", query = "select ie from InscricaoEvento ie where ie.referenciaCheckout = :referencia"),
        @NamedQuery(name = "InscricaoEvento.findReferenciasByStatusAndIgreja", query = "select ie.referenciaCheckout from InscricaoEvento ie where ie.membro.igreja.chave = :igreja and ie.status = :status group by ie.referenciaCheckout"),
        @NamedQuery(name = "InscricaoEvento.findMaxDataByEvento", query = "select max(ie.data) from InscricaoEvento ie where ie.evento.id = :evento and ie.evento.chaveIgreja = :igreja"),
        @NamedQuery(name = "InscricaoEvento.findAtivosByIgreja", query = "select ie from InscricaoEvento ie where ie.evento.tipo = :tipo and ie.chaveIgreja = :chaveIgreja and ie.evento.dataHoraTermino >= CURRENT_TIMESTAMP and ie.evento.status = :statusEvento and ie.status = :statusInscricao order by ie.data desc"),
        @NamedQuery(name = "InscricaoEvento.findInscricoesMembro", query = "select ie from InscricaoEvento ie where ie.evento.tipo = :tipo and ie.chaveIgreja = :igreja and lower(ie.emailInscrito) = :email and ie.evento.status = :statusEvento and ie.status in :statusInscricao")
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
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro membro;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_dispositivo", referencedColumnName = "chave")
    private Dispositivo dispositivo;

    @Id
    @JsonIgnore
    @Column(name = "id_evento", insertable = false, updatable = false)
    private Long idEvento;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_evento", referencedColumnName = "id_evento"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private Evento evento;

    @Setter
    @NotEmpty
    @Length(max = 150)
    @View.MergeViews(View.Cadastro.class)
    @Column(name = "nome_inscrito", length = 255, nullable = false)
    private String nomeInscrito;

    @NotEmpty
    @Length(max = 150)
    @Pattern(regexp = ".+@.+")
    @View.MergeViews(View.Cadastro.class)
    @Column(name = "email_inscrito", length = 150, nullable = false)
    private String emailInscrito;

    @Setter
    @NotEmpty
    @Length(max = 50)
    @View.MergeViews(View.Cadastro.class)
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

    @Setter
    @View.MergeViews(View.Cadastro.class)
    @OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValorInscricaoEvento> valores = new ArrayList<>();

    public InscricaoEvento(Evento evento) {
        this.evento = evento;
        this.valor = evento.getValor();
        if (!evento.isComPagamento()) {
            confirmada();
        }
    }

    public void setEmailInscrito(String emailInscrito) {
        if (emailInscrito != null) {
            this.emailInscrito = emailInscrito.trim();
        } else {
            this.emailInscrito = null;
        }
    }

    public String getValoresAdicionaisString() {
        StringBuilder valores = new StringBuilder();

        for (ValorInscricaoEvento val : this.valores) {
            if (valores.length() > 0) {
                valores.append(" | ");
            }

            valores.append(val.getNome()).append(": ").append(val.getValorFormatado());
        }

        return valores.toString();
    }

    public void confirmada() {
        status = StatusInscricaoEvento.CONFIRMADA;
    }

    public void cancelada() {
        status = StatusInscricaoEvento.CANCELADA;
    }

    public boolean isConfirmada() {
        return StatusInscricaoEvento.CONFIRMADA.equals(status);
    }

    public boolean isPendente() {
        return StatusInscricaoEvento.PENDENTE.equals(status);
    }

}
