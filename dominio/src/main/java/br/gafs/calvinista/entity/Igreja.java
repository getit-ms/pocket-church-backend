package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.StatusIgreja;
import br.gafs.calvinista.entity.domain.TemplateIgreja;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonView(Resumido.class)
    @Column(name = "chave_igreja")
    private String chave;
    
    @Setter
    @NotEmpty
    @Length(max = 150)
    @JsonView(Resumido.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @Setter
    @NotEmpty
    @Length(max = 15)
    @JsonView(View.Detalhado.class)
    @Column(name = "nome_aplicativo", length = 15, nullable = false)
    private String nomeAplicativo;
    
    @Setter
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Detalhado.class)
    @Column(name = "template", nullable = false)
    private TemplateIgreja template = TemplateIgreja._1;
    
    @Getter
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Detalhado.class)
    @Column(name = "status", nullable = false)
    private StatusIgreja status = StatusIgreja.ATIVO;
    
    @Setter
    @ManyToOne
    @JsonView(View.Detalhado.class)
    @JoinColumn(name = "id_plano", nullable = false)
    private Plano plano;
    
    @Column(name = "locale")
    @JsonView(View.Detalhado.class)
    private String locale;
    
    @Column(name = "timezone")
    @JsonView(View.Detalhado.class)
    private String timezone;
    
    @Setter
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @JsonView(View.Detalhado.class)
    @Column(name = "funcionalidade")
    @CollectionTable(name = "rl_igreja_funcionalidade_aplicativo",
            joinColumns = @JoinColumn(name = "chave_igreja"))
    private List<Funcionalidade> funcionalidadesAplicativo = new ArrayList<Funcionalidade>();
    
    @Transient
    @JsonView(View.Detalhado.class)
    private Membro primeiroMembro;

    @Override
    @JsonIgnore
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
    
    @JsonView(View.Detalhado.class)
    public boolean isAtiva(){
        return StatusIgreja.ATIVO.equals(status);
    }
    
    @JsonView(View.Detalhado.class)
    public boolean isBloqueada(){
        return StatusIgreja.BLOQUEADO.equals(status);
    }
    
    @JsonView(View.Detalhado.class)
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
