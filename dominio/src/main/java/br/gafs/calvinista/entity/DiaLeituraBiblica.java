/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@Table(name = "tb_dia_leitura_biblica")
@NamedQueries({
    @NamedQuery(name = "DiaLeituraBiblica.findByPlanoAndData", query = "select dlb from DiaLeituraBiblica dlb where dlb.plano.igreja.chave = :chaveIgreja and dlb.plano.id = :idPlano and dlb.data = :data")
})
public class DiaLeituraBiblica implements IEntity {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_dia_leitura_biblica")
    @GeneratedValue(generator = "seq_dia_leitura_biblica", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_dia_leitura_biblica", sequenceName = "seq_dia_leitura_biblica")
    private Long id;
    
    @Column(name = "data")
    @Temporal(TemporalType.DATE)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    private Date data;
    
    @Length(max = 200)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao", length = 200)
    private String descricao;
    
    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja"),
        @JoinColumn(name = "id_plano_leitura_biblica", referencedColumnName = "id_plano_leitura_biblica")
    })
    private PlanoLeituraBiblica plano;
    
}
