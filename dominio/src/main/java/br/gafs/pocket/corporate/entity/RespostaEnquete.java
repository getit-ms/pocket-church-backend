/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@IdClass(RespostaEnqueteId.class)
@Table(name = "tb_resposta_enquete")
@ToString(of = {"colaborador", "enquete"})
@EqualsAndHashCode(of = {"colaborador", "enquete"})
@NamedQueries({
    @NamedQuery(name = "RespostaEnquete.removerPorEnquete", query = "delete from RespostaEnquete rv where rv.enquete.id = :idEnquete and rv.empresa.chave = :chaveEmpresa")
})
public class RespostaEnquete implements IEntity {
    @Id
    @Column(name = "id_resposta_enquete")
    @SequenceGenerator(name = "seq_resposta_enquete", sequenceName = "seq_resposta_enquete")
    @GeneratedValue(generator = "seq_resposta_enquete", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Id
    @JsonIgnore
    @Column(name = "id_enquete", insertable = false, updatable = false)
    private Long idEnquete;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_enquete", referencedColumnName = "id_enquete", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", nullable = false)
    })
    private Enquete enquete;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data", nullable = false)
    private Date data = DateUtil.getDataAtual();
    
    @OneToMany(mappedBy = "enquete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaQuestao> respostas = new ArrayList<RespostaQuestao>();
    
    @ManyToOne
    @JoinColumn(name = "chave_empresa", insertable = false, updatable = false)
    private Empresa empresa;

    public RespostaEnquete(Enquete enquete) {
        this.enquete = enquete;
        for (Questao questao : enquete.getQuestoes()){
            respostas.add(new RespostaQuestao(this, questao));
        }
    }

    @Override
    public RespostaEnqueteId getId() {
        return new RespostaEnqueteId(id,
                    new RegistroEmpresaId(enquete.getEmpresa().getChave(), enquete.getId()));
    }
}
