/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.senha.SenhaUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@Table(name = "tb_usuario")
@NamedQueries({
    @NamedQuery(name = "Usuario.autentica", query = "select u from Usuario u where upper(u.login) = upper(:login) and upper(u.senha) = upper(:senha)")
})
public class Usuario implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_usuario")
    private Long id;
    
    @JsonIgnore
    @Column(name = "login", length = 150, unique = true)
    private String login;
    
    @Column(name = "nome", length = 150)
    private String nome;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @JsonIgnore
    @Column(name = "senha")
    private String senha;
    
    @Setter
    @Transient
    private String novaSenha;
    @Setter
    @Transient
    private String confirmacaoSenha;
    
    public void atualizaSenha(){
        if (!novaSenha.equals(confirmacaoSenha)){
            throw new ServiceException("mensagens.MSG-030");
        }
        
        this.senha = SenhaUtil.encryptSHA256(novaSenha);
    }
}
