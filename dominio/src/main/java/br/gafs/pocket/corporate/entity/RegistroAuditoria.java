/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusRegistroAuditoria;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
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
    
    @Column(name = "id_colaborador")
    private Long idColaborador;
    
    @Column(name = "chave_empresa")
    private String chaveEmpresa;
    
    @Column(name = "data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data = new Date();
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador", insertable = false, updatable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Colaborador colaborador;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa", insertable = false, updatable = false)
    private Empresa empresa;
    
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

    public RegistroAuditoria(String chaveDispositivo, Long idColaborador, String chaveEmpresa, String method) {
        this.chaveDispositivo = chaveDispositivo;
        this.idColaborador = idColaborador;
        this.chaveEmpresa = chaveEmpresa;
        this.method = method;
    }
    
}
