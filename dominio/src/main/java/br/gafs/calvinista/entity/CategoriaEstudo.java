package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

/**
 * Created by Gabriel on 15/02/2018.
 */
@Getter
@Entity
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_categoria_estudo")
@EqualsAndHashCode(of = {"id", "chaveIgreja"})
@NamedQueries({
        @NamedQuery(name = "CategoriaEstudo.findByIgreja", query = "select ce from CategoriaEstudo ce where ce.igreja.chave = :igreja order by ce.nome"),
        @NamedQuery(name = "CategoriaEstudo.findUsadasByIgreja", query = "select ce from Estudo e inner join e.categoria ce where e.igreja.chave = :igreja group by ce order by ce.nome"),
        @NamedQuery(name = "CategoriaEstudo.findByIgrejaAndNome", query = "select ce from CategoriaEstudo ce where ce.igreja.chave = :igreja and lower(ce.nome) = :nome")
})
public class CategoriaEstudo implements IEntity  {

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_categoria_estudo")
    @GeneratedValue(generator = "seq_categoria_estudo", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(sequenceName = "seq_categoria_estudo", name = "seq_categoria_estudo")
    private Long id;

    @Setter
    @NotEmpty
    @Length(max = 150)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Cadastro.class)
    @Column(name = "nome", length = 150, nullable = false, updatable = false)
    private String nome;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
}
