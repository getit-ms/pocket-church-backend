/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@IdClass(RespostaEnqueteColaboradorId.class)
@RequiredArgsConstructor
@Table(name = "tb_voto")
@NamedQueries({
    @NamedQuery(name = "RespostaEnqueteColaborador.removerPorEnquete", query = "delete from RespostaEnqueteColaborador v where v.enquete.id = :idEnquete and v.empresa.chave = :chaveEmpresa")
})
public class RespostaEnqueteColaborador implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_enquete", insertable = false, updatable = false)
    private Long idEnquete;
    
    @Id
    @JsonIgnore
    @Column(name = "id_colaborador", insertable = false, updatable = false)
    private Long idColaborador;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @NonNull
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_enquete", referencedColumnName = "id_enquete"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Enquete enquete;
    
    @NonNull
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa"),
    })
    private Colaborador colaborador;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa", insertable = false, updatable = false)
    private Empresa empresa;

    @Override
    public RespostaEnqueteColaboradorId getId() {
        return new RespostaEnqueteColaboradorId(
                new RegistroEmpresaId(empresa.getChave(), colaborador.getId()),
                new RegistroEmpresaId(empresa.getChave(), enquete.getId()));
    }
    
    
}
