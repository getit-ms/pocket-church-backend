/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusRegistroAuditoria;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gabriel
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "tb_registro_auditoria")
public class RegistroAuditoria implements IEntity {
    @Id
    @Column(name = "id_registro_auditoria")
    @GeneratedValue(generator = "seq_registro_auditoria", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(sequenceName = "seq_registro_auditoria", name = "seq_registro_auditoria")
    private Long id;

    @Column(name = "chave_dispositivo")
    private String chaveDispositivo;

    @Column(name = "id_membro")
    private Long idMembro;

    @Column(name = "chave_igreja")
    private String chaveIgreja;

    @Column(name = "data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data = new Date();

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro", insertable = false, updatable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro membro;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", insertable = false, updatable = false)
    private Igreja igreja;

    @Column(name = "method", length = 500)
    private String method;

    @Setter
    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Setter
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Setter
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusRegistroAuditoria status;

    public RegistroAuditoria(String chaveDispositivo, Long idMembro, String chaveIgreja, String method) {
        this.chaveDispositivo = chaveDispositivo;
        this.idMembro = idMembro;
        this.chaveIgreja = chaveIgreja;
        this.method = method;
    }

}
