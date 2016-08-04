package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.StatusIgreja;
import br.gafs.calvinista.entity.domain.TemplateIgreja;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "chave")
@Table(name = "tb_igreja")
@EqualsAndHashCode(of = "chave")
@NamedQueries({
    @NamedQuery(name = "Igreja.findAtivas", query = "select i from Igreja i where i.status in :status")
})
public class Igreja implements IEntity {
    @Id
    @Setter
    @Column(name = "chave_igreja")
    private String chave;
    
    @Setter
    @NotEmpty
    @Length(max = 150)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @Setter
    @NotEmpty
    @Length(max = 15)
    @Column(name = "nome_aplicativo", length = 15, nullable = false)
    private String nomeAplicativo;
    
    @Setter
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "template", nullable = false)
    private TemplateIgreja template = TemplateIgreja._1;
    
    @Getter
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusIgreja status = StatusIgreja.ATIVO;
    
    @Setter
    @ManyToOne
    @JoinColumn(name = "id_plano", nullable = false)
    private Plano plano;
    
    @Column(name = "locale")
    private String locale;
    
    @Column(name = "timezone")
    private String timezone;
    
    @Setter
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionalidade")
    @CollectionTable(name = "rl_igreja_funcionalidade_aplicativo",
            joinColumns = @JoinColumn(name = "chave_igreja"))
    private List<Funcionalidade> funcionalidadesAplicativo = new ArrayList<Funcionalidade>();
    
    @Transient
    private Membro primeiroMembro;

    @Override
    public String getId() {
        return chave;
    }
    
    public void ativa(){
        status = StatusIgreja.ATIVO;
    }
    
    public void inativa(){
        status = StatusIgreja.INATIVO;
    }
    
    public void bloqueia(){
        status = StatusIgreja.BLOQUEADO;
    }
    
    public boolean isAtiva(){
        return StatusIgreja.ATIVO.equals(status);
    }
    
    public boolean isBloqueada(){
        return StatusIgreja.BLOQUEADO.equals(status);
    }
    
    public boolean isInativa(){
        return StatusIgreja.INATIVO.equals(status);
    }

    public boolean possuiPermissao(Funcionalidade func) {
        if (func.isMembro()){
            return funcionalidadesAplicativo.contains(func);
        }
        
        return plano.getFuncionalidades().contains(func);
    }
}
