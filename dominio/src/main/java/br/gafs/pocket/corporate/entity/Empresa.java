package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.StatusEmpresa;
import br.gafs.pocket.corporate.view.View;
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
@Table(name = "tb_empresa")
@EqualsAndHashCode(of = "chave")
@NamedQueries({
    @NamedQuery(name = "Empresa.findAtivas", query = "select i from Empresa i where i.status in :status"),
    @NamedQuery(name = "Empresa.findFuncionalidadesInList", query = "select f from Empresa i inner join i.plano p inner join p.funcionalidades f where i.chave = :empresa and f in :list group by f"),
    @NamedQuery(name = "Empresa.findFuncionalidadesAplicativoInList", query = "select f from Empresa i inner join i.funcionalidadesAplicativo f where i.chave = :empresa and f in :list group by f"),
    @NamedQuery(name = "Empresa.findByEmailAcesso", query = "select new br.gafs.pocket.corporate.dto.ResumoEmpresaDTO(e.chave, e.nome, e.nomeAplicativo, temp.logoPequena) from Template temp, Colaborador c inner join c.acesso a inner join c.empresa e where e = temp.empresa and e.status = :statusEmpresa and c.status = :statusColaborador and lower(c.email) = :email")
})
public class Empresa implements IEntity {
    @Id
    @Setter
    @JsonView(View.Resumido.class)
    @Column(name = "chave_empresa")
    private String chave;
    
    @Setter
    @NotEmpty
    @Length(max = 150)
    @JsonView(View.Resumido.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @Setter
    @NotEmpty
    @Length(max = 35)
    @JsonView(View.Resumido.class)
    @Column(name = "nome_aplicativo", length = 35, nullable = false)
    private String nomeAplicativo;
    
    @Getter
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Detalhado.class)
    @Column(name = "status", nullable = false)
    private StatusEmpresa status = StatusEmpresa.ATIVO;
    
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
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @JsonView(View.Detalhado.class)
    @Column(name = "funcionalidade")
    @CollectionTable(name = "rl_empresa_funcionalidade_aplicativo",
            joinColumns = @JoinColumn(name = "chave_empresa"))
    private List<Funcionalidade> funcionalidadesAplicativo = new ArrayList<Funcionalidade>();
    
    @Transient
    @JsonView(View.Detalhado.class)
    private Colaborador primeiroColaborador;

    @Override
    @JsonIgnore
    public String getId() {
        return chave;
    }
    
    public void ativa(){
        status = StatusEmpresa.ATIVO;
    }
    
    public void inativa(){
        status = StatusEmpresa.INATIVO;
    }
    
    public void bloqueia(){
        status = StatusEmpresa.BLOQUEADO;
    }
    
    @JsonView(View.Detalhado.class)
    public boolean isAtiva(){
        return StatusEmpresa.ATIVO.equals(status);
    }
    
    @JsonView(View.Detalhado.class)
    public boolean isBloqueada(){
        return StatusEmpresa.BLOQUEADO.equals(status);
    }
    
    @JsonView(View.Detalhado.class)
    public boolean isInativa(){
        return StatusEmpresa.INATIVO.equals(status);
    }

    public boolean possuiPermissao(Funcionalidade func) {
        if (func.isColaborador()){
            return funcionalidadesAplicativo.contains(func);
        }
        
        return plano.getFuncionalidades().contains(func);
    }
}
