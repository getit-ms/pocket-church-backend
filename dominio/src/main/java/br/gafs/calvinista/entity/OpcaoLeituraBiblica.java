/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tb_opcao_leitura_biblica")
@NamedQueries({
    @NamedQuery(name = "OpcaoLeituraBiblica.findOpcaoSelecionad", query = "select olb from OpcaoLeituraBiblica olb where olb.membro.id = :idMembro and olb.membro.igreja.chave = :chaveIgreja and olb.dataTermino is null"),
    @NamedQuery(name = "OpcaoLeituraBiblica.removeByPlano", query = "delete from OpcaoLeituraBiblica olb where olb.planoLeitura.igreja.chave = :chaveIgreja and olb.planoLeitura.id = :idPlano")
})
public class OpcaoLeituraBiblica implements IEntity {
    @Id
    @Column(name = "id_opcao_leitura_biblica")
    @GeneratedValue(generator = "seq_opcao_leitura_biblica", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_opcao_leitura_biblica", sequenceName = "seq_opcao_leitura_biblica")
    private Long id;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    })
    private Membro membro;
    
    @Column(name = "inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInicio = new Date();
    
    @Column(name = "termino")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataTermino;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_plano_leitura_biblica", referencedColumnName = "id_plano_leitura_biblica"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private PlanoLeituraBiblica planoLeitura;

    public OpcaoLeituraBiblica(Membro membro, PlanoLeituraBiblica planoLeitura) {
        this.membro = membro;
        this.planoLeitura = planoLeitura;
    }
    
    public void encerra(){
        dataTermino = new Date();
    }
}
