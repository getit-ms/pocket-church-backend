/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TestamentoBiblia;
import br.gafs.calvinista.entity.domain.VersiculoBiblia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@IdClass(LivroBibliaId.class)
@Table(name = "tb_livro_biblia")
@ToString(of = {"id", "idBiblia"})
@EqualsAndHashCode(of = {"id", "idBiblia"})
public class LivroBiblia implements IEntity {
    @Id
    @Column(name = "id_livro_biblia")
    @SequenceGenerator(name = "seq_livro_biblia", sequenceName = "seq_livro_biblia")
    @GeneratedValue(generator = "seq_livro_biblia", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "id_biblia", insertable = false, updatable = false)
    private Long idBiblia;
    
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
    @JsonIgnore
    @JoinColumn(name = "id_biblia")
    private Biblia biblia;
    
    @OneToMany
    @OrderBy("capitulo, versiculo")
    @JoinColumns({
            @JoinColumn(name = "id_livro_biblia", referencedColumnName = "id_livro_biblia"),
            @JoinColumn(name = "id_biblia", referencedColumnName = "id_biblia")
    })
    private List<VersiculoBiblia> versiculos = new ArrayList<VersiculoBiblia>();
}
