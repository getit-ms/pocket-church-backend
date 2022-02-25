/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusPedidoOracao;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_pedido_oracao")
public class PedidoOracao implements IEntity {
    @Id
    @Column(name = "id_pedido_oracao")
    @SequenceGenerator(sequenceName = "seq_pedido_oracao", name = "seq_pedido_oracao")
    @GeneratedValue(generator = "seq_pedido_oracao", strategy = GenerationType.SEQUENCE)
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
    @Column(name = "pedido", length = 500, nullable = false)
    private String pedido;

    @NotNull
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusPedidoOracao status = StatusPedidoOracao.PENDENTE;

    @Setter
    @NotNull
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_solicitante", referencedColumnName = "id_membro", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false, insertable = false, updatable = false)
    })
    private Membro solicitante;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_atendente", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false, insertable = false, updatable = false)
    })
    private Membro atendente;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public boolean isAtendido() {
        return StatusPedidoOracao.ATENDIDO.equals(status);
    }

    public boolean isPendente() {
        return StatusPedidoOracao.PENDENTE.equals(status);
    }

    public void atende(Membro membro) {
        if (isPendente()) {
            status = StatusPedidoOracao.ATENDIDO;
            dataAtendimento = DateUtil.getDataAtual();
            atendente = membro;
        }
    }

    public void setSolicitante(Membro solicitante) {
        this.solicitante = solicitante;
        this.igreja = solicitante.getIgreja();
    }
}
