/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusMensagemDia;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@Table(name = "tb_mensagem_dia")
@NamedQueries({
    @NamedQuery(name = "MensagemDia.findByEmpresaAndStatus", query = "select vd from MensagemDia vd where vd.empresa.chave = :chaveEmpresa and vd.status = :status"),
    @NamedQuery(name = "MensagemDia.sorteiaByEmpresa", query = "select vd from MensagemDia vd where vd.empresa.chave = :idEmpresa and vd.status = :status order by vd.envios, mod(vd.id, :rand), vd.id"),
    @NamedQuery(name = "MensagemDia.findMenorEnvioByEmpresaAndStatus", query = "select min(vd.envios) from MensagemDia vd where vd.empresa.chave = :empresa and vd.status = :status")
})
public class MensagemDia implements IEntity {
    @Id
    @Column(name = "id_vericulo_diario")
    @GeneratedValue(generator = "seq_vericulo_diario", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_vericulo_diario", sequenceName = "seq_vericulo_diario")
    private Long id;
    
    @NotEmpty
    @Length(max = 250)
    @Column(name = "mensagem", nullable = false, length = 250)
    private String mensagem;
    
    @Enumerated(EnumType.ORDINAL)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "status", nullable = false)
    private StatusMensagemDia status = StatusMensagemDia.HABILITADO;
    
    @JsonIgnore
    @Column(name = "envios", nullable = false)
    private Integer envios = 0;
    
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultimo_envio")
    private Date ultimoEnvio = new Date();
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    public MensagemDia(Empresa empresa) {
        this.empresa = empresa;
    }
    
    public boolean isHabilitado(){
        return StatusMensagemDia.HABILITADO.equals(status);
    }
    
    public void habilitado(){
        status = StatusMensagemDia.HABILITADO;
    }
    
    public void desabilitado(){
        status = StatusMensagemDia.DESABILITADO;
    }
    
    public void ativo(){
        status = StatusMensagemDia.ATIVO;
        ultimoEnvio = new Date();
        envios++;
    }
    
    public boolean isDesabilitado(){
        return StatusMensagemDia.DESABILITADO.equals(status);
    }
    
    public boolean isAtivo(){
        return StatusMensagemDia.ATIVO.equals(status);
    }
}
