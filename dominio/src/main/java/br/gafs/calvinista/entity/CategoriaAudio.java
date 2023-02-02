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
 * Created by Gabriel on 23/07/2018.
 */

@Getter
@Entity
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_categoria_audio")
@EqualsAndHashCode(of = {"id", "chaveIgreja"})
@NamedQueries({
        @NamedQuery(name = "CategoriaAudio.findByIgreja", query = "select ca from CategoriaAudio ca where ca.igreja.chave = :igreja order by ca.nome"),
        @NamedQuery(name = "CategoriaAudio.findUsadasByIgreja", query = "select ca from Audio a inner join a.categoria ca where a.igreja.chave = :igreja group by ca order by ca.nome"),
        @NamedQuery(name = "CategoriaAudio.findByIgrejaAndNome", query = "select ca from CategoriaAudio ca where ca.igreja.chave = :igreja and lower(ca.nome) = :nome")
})
public class CategoriaAudio implements IEntity {

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_categoria_audio")
    @GeneratedValue(generator = "seq_categoria_audio", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(sequenceName = "seq_categoria_audio", name = "seq_categoria_audio")
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
