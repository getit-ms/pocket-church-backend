/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@IdClass(AcessoId.class)
@ToString(of = "membro")
@Table(name = "tb_acesso")
@EqualsAndHashCode(of = "membro")
public class Acesso implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_membro", insertable = false, updatable = false)
    private Long idMembro;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @OneToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private Membro membro;

    @ManyToMany
    @JoinTable(name = "rl_acesso_perfil",
            joinColumns = {
                    @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
                    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "id_perfil", referencedColumnName = "id_perfil"),
                    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
            })
    private List<Perfil> perfis = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "rl_acesso_ministerio",
            joinColumns = {
                    @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
                    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "id_ministerio", referencedColumnName = "id_ministerio"),
                    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
            })
    private List<Ministerio> ministerios = new ArrayList<>();

    public Acesso(Membro membro) {
        this.membro = membro;
        this.membro.setAcesso(this);
    }

    public boolean isExigeMinisterios() {
        for (Perfil perfil : perfis) {
            if (perfil.isExigeMinisterios()) {
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
    public AcessoId getId() {
        return new AcessoId(new RegistroIgrejaId(membro.getIgreja().getChave(), membro.getId()));
    }

    public boolean possuiPermissao(Funcionalidade func) {
        if (membro.getIgreja().possuiPermissao(func)) {
            for (Perfil perfil : perfis) {
                if (perfil.getFuncionalidades().contains(func)) {
                    return true;
                }
            }
        }
        return false;
    }
}
