/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoParametro;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Gabriel
 */
@Getter
@Entity
@Cacheable
@NoArgsConstructor
@IdClass(ParametroId.class)
@Table(name = "tb_parametro")
public class Parametro implements IEntity {
    public static final String GLOBAL = "_GLOBAL_";

    @Id
    @Column(name = "grupo")
    private String grupo;

    @Id
    @Column(name = "chave")
    @Enumerated(EnumType.STRING)
    private TipoParametro chave;

    @Setter
    @Column(name = "valor")
    private String valor;

    @Lob
    @Setter
    @Column(name = "anexo")
    private byte[] anexo;

    @Override
    public ParametroId getId() {
        return new ParametroId(grupo, chave);
    }

    public Parametro(String grupo, TipoParametro chave) {
        this.grupo = grupo;
        this.chave = chave;
    }

    public <T> T get() {
        return chave.get(this);
    }

    public <T> void set(T valor) {
        chave.set(this, valor);
    }
}
