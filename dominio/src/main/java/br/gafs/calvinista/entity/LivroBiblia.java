/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TestamentoBiblia;
import br.gafs.calvinista.entity.domain.VersiculoBiblia;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_livro_biblia")
public class LivroBiblia implements IEntity {
    @Id
    @Column(name = "id_livro_biblia")
    @SequenceGenerator(name = "seq_livro_biblia", sequenceName = "seq_livro_biblia")
    @GeneratedValue(generator = "seq_livro_biblia", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Column(name = "nome", length = 50)
    private String nome;
    
    @Column(name = "ordem")
    private Integer ordem;
    
    @Column(name = "abreviacao", length = 5)
    private String abreviacao;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_atualizacao")
    private Date ultimaAtualizacao = new Date();
    
    @Column(name = "testamento")
    @Enumerated(EnumType.ORDINAL)
    private TestamentoBiblia testamento;
    
    @ManyToOne
    @JoinColumn(name = "id_biblia")
    private Biblia biblia;
    
    @OneToMany
    @OrderBy("capitulo, versiculo")
    @JoinColumn(name = "id_livro_biblia")
    private List<VersiculoBiblia> versiculos = new ArrayList<VersiculoBiblia>();
}
