package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.StatusIgreja;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Resumido;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
@Cacheable
@ToString(of = "chave")
@Table(name = "tb_igreja")
@EqualsAndHashCode(of = "chave")
@NamedQueries({
    @NamedQuery(name = "Igreja.findAtivas", query = "select i from Igreja i where i.status in :status"),
    @NamedQuery(name = "Igreja.findFuncionalidadesInList", query = "select f from Igreja i inner join i.plano p inner join p.funcionalidades f where i.chave = :igreja and f in :list group by f"),
    @NamedQuery(name = "Igreja.findFuncionalidadesAplicativoInList", query = "select f from Igreja i inner join i.funcionalidadesAplicativo f where i.chave = :igreja and f in :list group by f"),
    @NamedQuery(name = "Igreja.findByEmailAcesso", query = "select new br.gafs.calvinista.dto.ResumoIgrejaDTO(i.chave, i.nome, i.nomeAplicativo, temp.logoPequena) from Template temp, Membro m inner join m.acesso a inner join m.igreja i where i = temp.igreja and i.status = :statusIgreja and m.status = :statusMembro and lower(m.email) = :email")
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
    @JsonView(View.Resumido.class)
    @Column(name = "nome_aplicativo", length = 15, nullable = false)
    private String nomeAplicativo;

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

    @Setter
    @Column(name = "timezone")
    @JsonView(View.Detalhado.class)
    private String timezone;

    @Setter
    @JsonIgnore
    @Column(name = "agrupamento")
    private String agrupamento;
    
    @ManyToOne
    @JoinColumn(name = "id_biblia")
    private Biblia biblia;
    
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
