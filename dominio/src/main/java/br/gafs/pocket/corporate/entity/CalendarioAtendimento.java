/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusCalendario;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
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
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@Table(name = "tb_calendario_atendimento")
@NamedQueries({
    @NamedQuery(name = "CalendarioAtendimento.findByEmpresa",
            query = "select ca from CalendarioAtendimento ca where ca.gerente.empresa.chave = :idEmpresa and ca.status = :status order by ca.gerente.nome"),
    @NamedQuery(name = "CalendarioAtendimeto.findByGerenteAndEmpresaAndStatus",
            query = "select ca from CalendarioAtendimento ca where ca.empresa.chave = :empresa and ca.status = :status and ca.gerente.id = :gerente")
})
public class CalendarioAtendimento implements IEntity {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_calendario_atendimento")
    @GeneratedValue(generator = "seq_calendario_atendimento", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_calendario_atendimento", sequenceName = "seq_calendario_atendimento")
    private Long id;
    
    @JsonIgnore
    @JsonView(View.Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusCalendario status = StatusCalendario.ATIVO;
    
    @OneToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
        @JoinColumn(name = "id_gerente", referencedColumnName = "id_colaborador", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false, insertable = false, updatable = false)
    })
    private Colaborador gerente;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @OneToOne
    @JsonIgnore
    @XmlTransient
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;

    public CalendarioAtendimento(Colaborador gerente) {
        this.gerente = gerente;
    }
    
    public void inativa(){
        status = StatusCalendario.INATIVO;
    }
    
    public void ativa(){
        status = StatusCalendario.ATIVO;
    }
    
    @JsonView(View.Resumido.class)
    public boolean isAtivo(){
        return StatusCalendario.ATIVO.equals(status);
    }
    
    @JsonView(View.Resumido.class)
    public boolean isInativo(){
        return StatusCalendario.INATIVO.equals(status);
    }
}
