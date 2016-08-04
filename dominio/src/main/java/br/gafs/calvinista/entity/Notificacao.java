/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
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
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_notificacao")
@IdClass(RegistroIgrejaId.class)
public class Notificacao implements IEntity {
    @Id
    @Column(name = "id_notificacao")
    @SequenceGenerator(sequenceName = "seq_notificacao", name = "seq_notificacao")
    @GeneratedValue(generator = "seq_notificacao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 150)
    @Column(name = "mensagem", length = 150, nullable = false, updatable = false)
    private String mensagem;
    
    @ManyToMany
    @JoinTable(name = "rl_notificacao_ministerio_alvo",
            joinColumns = {
                @JoinColumn(name = "id_notificacao", referencedColumnName = "id_notificacao"),
                @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
            },
            inverseJoinColumns = {
                @JoinColumn(name = "id_ministerio", referencedColumnName = "id_ministerio"),
                @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
            })
    private List<Ministerio> ministeriosAlvo = new ArrayList<Ministerio>();
    
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    public Notificacao(Igreja igreja) {
        this.igreja = igreja;
    }
    
    public void addAlvo(Ministerio grupo){
        ministeriosAlvo.add(grupo);
    }
    
    public void addAlvos(List<Ministerio> grupos){
        ministeriosAlvo.addAll(grupos);
    }
    
}
