/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusVersiculoDiario;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_versiculo_diario")
@NamedQueries({
        @NamedQuery(name = "VersiculoDiario.findByIgrejaAndStatus", query = "select vd from VersiculoDiario vd where vd.igreja.chave = :chaveIgreja and vd.status = :status"),
        @NamedQuery(name = "VersiculoDiario.sorteiaByIgreja", query = "select vd from VersiculoDiario vd where vd.igreja.chave = :idIgreja and vd.status = :status order by vd.envios, mod(vd.id, :rand), vd.id"),
        @NamedQuery(name = "VersiculoDiario.findMenorEnvioByIgrejaAndStatus", query = "select min(vd.envios) from VersiculoDiario vd where vd.igreja.chave = :igreja and vd.status = :status")
})
public class VersiculoDiario implements IEntity {
    @Id
    @Column(name = "id_vericulo_diario")
    @GeneratedValue(generator = "seq_vericulo_diario", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_vericulo_diario", sequenceName = "seq_vericulo_diario")
    private Long id;

    @NotEmpty
    @Length(max = 250)
    @Column(name = "versiculo", nullable = false, length = 250)
    private String versiculo;

    @Enumerated(EnumType.ORDINAL)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "status", nullable = false)
    private StatusVersiculoDiario status = StatusVersiculoDiario.HABILITADO;

    @JsonIgnore
    @Column(name = "envios", nullable = false)
    private Integer envios = 0;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultimo_envio")
    private Date ultimoEnvio = new Date();

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public VersiculoDiario(Igreja igreja) {
        this.igreja = igreja;
    }

    public boolean isHabilitado() {
        return StatusVersiculoDiario.HABILITADO.equals(status);
    }

    public void habilitado() {
        status = StatusVersiculoDiario.HABILITADO;
    }

    public void desabilitado() {
        status = StatusVersiculoDiario.DESABILITADO;
    }

    public void ativo() {
        status = StatusVersiculoDiario.ATIVO;
        ultimoEnvio = new Date();
        envios++;
    }

    public boolean isDesabilitado() {
        return StatusVersiculoDiario.DESABILITADO.equals(status);
    }

    public boolean isAtivo() {
        return StatusVersiculoDiario.ATIVO.equals(status);
    }
}
