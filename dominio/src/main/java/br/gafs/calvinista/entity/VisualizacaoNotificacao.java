/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VisualizacaoNotificacaoId.class)
@Table(name = "rl_dispositivo_notificacao")
public class VisualizacaoNotificacao implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "chave_dispositivo", insertable = false, updatable = false)
    private String chaveDispositivo;
    
    @ManyToOne
    @JoinColumn(name = "chave_dispositivo")
    private Dispositivo dispositivo;
    
    @Id
    @JsonIgnore
    @Column(name = "id_notificacao", insertable = false, updatable = false)
    private Long idNotificacao;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_notificacao", referencedColumnName = "id_notificacao"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private Notificacao notificacao;
            
    @Override
    public VisualizacaoNotificacaoId getId() {
        return new VisualizacaoNotificacaoId(
                dispositivo.getChave(), new RegistroIgrejaId(notificacao.getIgreja().getChave(), notificacao.getId()));
    }
    
}
