/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "login")
@EqualsAndHashCode(of = "login")
@Table(name = "tb_administrador")
public class Administrador implements IEntity {
    @Id
    @Setter
    @NotEmpty
    @Length(max = 150)
    @Column(name = "login", nullable = false, length = 150)
    private String login;

    @Setter
    @NotEmpty
    @Length(max = 250)
    @Column(name = "senha", nullable = false, length = 250)
    private String senha;

    @Override
    public String getId() {
        return login;
    }


}
