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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@IdClass(VotoId.class)
@RequiredArgsConstructor
@Table(name = "tb_voto")
@NamedQueries({
    @NamedQuery(name = "Voto.removerPorVotacao", query = "delete from Voto v where v.votacao.id = :idVotacao and v.igreja.chave = :chaveIgreja")
})
public class Voto implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_votacao", insertable = false, updatable = false)
    private Long idVotacao;
    
    @Id
    @JsonIgnore
    @Column(name = "id_membro", insertable = false, updatable = false)
    private Long idMembro;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @NonNull
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_votacao", referencedColumnName = "id_votacao"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Votacao votacao;
    
    @NonNull
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja"),
    })
    private Membro membro;
    
    @ManyToOne
    @JoinColumn(name = "chave_igreja", insertable = false, updatable = false)
    private Igreja igreja;

    @Override
    public VotoId getId() {
        return new VotoId(
                new RegistroIgrejaId(igreja.getChave(), membro.getId()), 
                new RegistroIgrejaId(igreja.getChave(), votacao.getId()));
    }
    
    
}
