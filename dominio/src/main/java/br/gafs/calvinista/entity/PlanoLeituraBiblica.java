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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_plano_leitura_biblica")
public class PlanoLeituraBiblica implements IEntity {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_plano_leitura_biblica")
    @GeneratedValue(generator = "seq_plano_leitura_biblica", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seq_plano_leitura_biblica", sequenceName = "seq_plano_leitura_biblica")
    private Long id;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @Length(max = 150)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao", length = 150)
    private String descricao;
    
    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;
    
    @Setter
    @OrderBy("data")
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaLeituraBiblica> dias = new ArrayList<DiaLeituraBiblica>();
    
    @JsonView(View.Resumido.class)
    public Date getDataInicio(){
        if (dias.isEmpty()){
            return null;
        }
        
        return dias.get(0).getData();
    }
    
    @JsonView(View.Resumido.class)
    public Date getDataTermino(){
        if (dias.isEmpty()){
            return null;
        }
        
        return dias.get(dias.size() - 1).getData();
    }
}
