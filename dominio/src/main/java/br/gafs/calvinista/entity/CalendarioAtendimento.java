/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusCalendario;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_calendario_atendimento")
@NamedQueries({
        @NamedQuery(name = "CalendarioAtendimento.findByIgreja",
                query = "select ca from CalendarioAtendimento ca where ca.pastor.igreja.chave = :idIgreja and ca.status = :status order by ca.pastor.nome"),
        @NamedQuery(name = "CalendarioAtendimeto.findByPastorAndIgrejaAndStatus",
                query = "select ca from CalendarioAtendimento ca where ca.igreja.chave = :igreja and ca.status = :status and ca.pastor.id = :pastor")
})
public class CalendarioAtendimento implements IEntity {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_calendario_atendimento")
    @GeneratedValue(generator = "seq_calendario_atendimento", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_calendario_atendimento", sequenceName = "seq_calendario_atendimento")
    private Long id;

    @JsonIgnore
    @JsonView(Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusCalendario status = StatusCalendario.ATIVO;

    @OneToOne
    @JsonView(Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_pastor", referencedColumnName = "id_membro", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", nullable = false, insertable = false, updatable = false)
    })
    private Membro pastor;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @OneToOne
    @JsonIgnore
    @XmlTransient
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    public CalendarioAtendimento(Membro pastor) {
        this.pastor = pastor;
    }

    public void inativa() {
        status = StatusCalendario.INATIVO;
    }

    public void ativa() {
        status = StatusCalendario.ATIVO;
    }

    @JsonView(Resumido.class)
    public boolean isAtivo() {
        return StatusCalendario.ATIVO.equals(status);
    }

    @JsonView(Resumido.class)
    public boolean isInativo() {
        return StatusCalendario.INATIVO.equals(status);
    }
}
