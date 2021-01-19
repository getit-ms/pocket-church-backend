/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@IdClass(AcessoId.class)
@ToString(of = "colaborador")
@Table(name = "tb_acesso")
@EqualsAndHashCode(of = "colaborador")
public class Acesso implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_colaborador", insertable = false, updatable = false)
    private Long idColaborador;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
            
    @OneToOne
    @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
    })
    private Colaborador colaborador;
    
    @ManyToMany
    @JoinTable(name = "rl_acesso_perfil",
            joinColumns = {
                @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
                @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
            },
            inverseJoinColumns = {
                @JoinColumn(name = "id_perfil", referencedColumnName = "id_perfil"),
                @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
            })
    private List<Perfil> perfis = new ArrayList<>();
    
    public Acesso(Colaborador colaborador) {
        this.colaborador = colaborador;
        this.colaborador.setAcesso(this);
    }
    
    public boolean isExigeMinisterios(){
        for (Perfil perfil : perfis){
            if (perfil.isExigeMinisterios()){
                return true;
            }
        }
        return false;
    }

    public void removePerfil(Perfil perfil) {
        perfis.remove(perfil);
    }

    public void addPerfil(Perfil perfil) {
        perfis.add(perfil);
    }
    
    @Override
    @JsonIgnore
    public AcessoId getId(){
        return new AcessoId(new RegistroEmpresaId(colaborador.getEmpresa().getChave(), colaborador.getId()));
    }

    public boolean possuiPermissao(Funcionalidade func) {
        if (colaborador.getEmpresa().possuiPermissao(func)){
            for (Perfil perfil : perfis){
                if (perfil.getFuncionalidades().contains(func)){
                    return true;
                }
            }
        }
        return false;
    }
}
