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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.Getter;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@IdClass(RegistroEmpresaId.class)
@Table(name = "vw_aniversario_colaborador")
@NamedQueries({
    @NamedQuery(name = "AniversarioColaborador.findAniversariantes", query = "select m from AniversarioColaborador am, Colaborador m where m.status <> :status and m.id = am.id and m.empresa = am.empresa and am.aniversariante = true and am.empresa.chave = :empresa"),
    @NamedQuery(name = "AniversarioColaborador.findProximosAniversariantes", query = "select m from AniversarioColaborador am, Colaborador m where m.status <> :status and m.id = am.id and m.empresa = am.empresa and m.desejaDisponibilizarDados = true and m.dadosDisponiveis = true and am.empresa.chave = :empresa and (am.mes * 100 + am.dia) between :inicio and :fim order by am.mes, am.dia, m.nome")
})
public class AniversarioColaborador implements IEntity {
    @Id
    @Column(name = "id_colaborador")
    private Long id;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;
    
    @Column(name = "dia")
    private Integer dia;
    
    @Column(name = "mes")
    private Integer mes;
    
    @Column(name = "aniversariante")
    private boolean aniversariante;
    
    public RegistroEmpresaId getId(){
        return new RegistroEmpresaId(empresa.getChave(), id);
    }
}
