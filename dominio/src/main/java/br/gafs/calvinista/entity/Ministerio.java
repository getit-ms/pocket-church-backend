/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusMinisterio;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_ministerio")
@IdClass(RegistroIgrejaId.class)
@NamedQueries({
    @NamedQuery(name = "Ministerio.findByStatusAndIgreja", query = "select m from Ministerio m where m.igreja.chave = :idIgreja and m.status = :status order by m.nome"),
    @NamedQuery(name = "Ministerio.findByStatusAndIgrejaAndId", query = "select m from Ministerio m where m.id = :idMinisterio and m.igreja.chave = :idIgreja and m.status = :status order by m.nome"),
    @NamedQuery(name = "Ministerio.findByAcesso", query = "select mi from Membro me inner join me.acesso ac inner join ac.ministerios mi where me.id = :idMembro and me.igreja.chave = :chaveIgreja and mi.status = :status order by mi.nome"),
    @NamedQuery(name = "Ministerio.findByIgrejaAndStatus", query = "select m from Ministerio m where m.igreja.chave = :chaveIgreja and m.status = :status order by m.nome")
})
public class Ministerio implements IEntity {
    @Id
    @Column(name = "id_ministerio")
    @SequenceGenerator(sequenceName = "seq_ministerio", name = "seq_ministerio")
    @GeneratedValue(generator = "seq_ministerio", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Setter
    @NotEmpty
    @Length(max = 150)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;
    
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusMinisterio status = StatusMinisterio.ATIVO;
    
    @Id
    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false, updatable = false)
    private Igreja igreja;

    public Ministerio(Igreja igreja) {
        this.igreja = igreja;
    }
    
    public boolean isAtivo(){
        return StatusMinisterio.ATIVO.equals(status);
    }
    
    public void ativa(){
        status = StatusMinisterio.ATIVO;
    }
    
    public void inativa(){
        status = StatusMinisterio.INATIVO;
    }
}
