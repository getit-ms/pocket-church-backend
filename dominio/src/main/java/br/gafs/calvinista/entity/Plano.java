/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.StatusPlano;
import br.gafs.util.date.DateUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "id")
@Table(name = "tb_plano")
@EqualsAndHashCode(of = "id")
public class Plano {
    @Id
    @Column(name = "id_plano")
    @SequenceGenerator(name = "seq_plano", sequenceName = "seq_plano")
    @GeneratedValue(generator = "seq_plano", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Setter
    @NotEmpty
    @Length(max = 150)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @Past
    @Column(name = "data_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInicio = DateUtil.getDataAtual();

    @Column(name = "data_termino")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataTermino;

    @Column(name = "limite_audios")
    private long limiteAudios;

    @Setter
    @NotNull
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = true)
    private StatusPlano status = StatusPlano.ATIVO;

    @Setter
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionalidade")
    @CollectionTable(name = "rl_plano_funcionalidade",
            joinColumns = @JoinColumn(name = "id_plano"))
    private List<Funcionalidade> funcionalidades = new ArrayList<Funcionalidade>();

    public void desliga() {
        if (isAtivo()) {
            status = StatusPlano.INATIVO;
            dataTermino = DateUtil.getDataAtual();
        }
    }

    public boolean isAtivo() {
        return StatusPlano.ATIVO.equals(status);
    }

    public boolean isInativo() {
        return StatusPlano.INATIVO.equals(status);
    }

    public List<Funcionalidade> getFuncionalidadesAdmin() {
        List<Funcionalidade> admin = new ArrayList<Funcionalidade>();

        for (Funcionalidade func : funcionalidades) {
            if (func.isAdmin()) {
                admin.add(func);
            }
        }

        return admin;
    }

    public List<Funcionalidade> getFuncionalidadesMembro() {
        List<Funcionalidade> admin = new ArrayList<Funcionalidade>();

        for (Funcionalidade func : funcionalidades) {
            if (func.isMembro() || func.isPublica()) {
                admin.add(func);
            }
        }

        return admin;
    }
}
