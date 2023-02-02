/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.JsonTemporal;
import br.gafs.calvinista.view.View.JsonTemporalType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
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

    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_alteracao")
    private Date ultimaAlteracao = new Date();

    @Setter
    @OrderBy("data")
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaLeituraBiblica> dias = new ArrayList<DiaLeituraBiblica>();

    @JsonView(View.Resumido.class)
    @JsonTemporal(JsonTemporalType.DATE)
    public Date getDataInicio() {
        if (dias.isEmpty()) {
            return null;
        }

        return dias.get(0).getData();
    }

    @JsonView(View.Resumido.class)
    @JsonTemporal(JsonTemporalType.DATE)
    public Date getDataTermino() {
        if (dias.isEmpty()) {
            return null;
        }

        return dias.get(dias.size() - 1).getData();
    }

    public void alterado() {
        ultimaAlteracao = new Date();
    }
}
