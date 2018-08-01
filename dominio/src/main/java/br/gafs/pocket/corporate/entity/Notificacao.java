/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

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
@IdClass(RegistroEmpresaId.class)
public class Notificacao implements IEntity {
    @Id
    @Column(name = "id_notificacao")
    @SequenceGenerator(sequenceName = "seq_notificacao", name = "seq_notificacao")
    @GeneratedValue(generator = "seq_notificacao", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 30)
    @Column(name = "titulo", length = 30, nullable = false, updatable = false)
    private String titulo;

    @NotEmpty
    @Length(max = 150)
    @Column(name = "mensagem", length = 150, nullable = false, updatable = false)
    private String mensagem;

    @Column(name = "apenas_colaboradores", nullable = false, updatable = false)
    private boolean apenasColaboradores;
    
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data", nullable = false, updatable = false)
    private Date data = new Date();

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;

    public Notificacao(Empresa empresa) {
        this.empresa = empresa;
    }

}
