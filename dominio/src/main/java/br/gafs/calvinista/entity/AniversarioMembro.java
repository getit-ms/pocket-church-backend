/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.Getter;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@IdClass(RegistroIgrejaId.class)
@Table(name = "vw_aniversario_membro")
@NamedQueries({
    @NamedQuery(name = "AniversarioMembro.findAniversariantes", query = "select m from AniversarioMembro am, Membro m where m.id = am.id and m.igreja = am.igreja and am.aniversariante = true and am.igreja.chave = :igreja")
})
public class AniversarioMembro implements IEntity {
    @Id
    @Column(name = "id_membro")
    private Long id;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @ManyToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;
    
    @Column(name = "dia")
    private Integer dia;
    
    @Column(name = "mes")
    private Integer mes;
    
    @Column(name = "aniversariante")
    private boolean aniversariante;
    
    public RegistroIgrejaId getId(){
        return new RegistroIgrejaId(igreja.getChave(), id);
    }
}
