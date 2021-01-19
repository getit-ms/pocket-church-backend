package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.view.View;
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
@IdClass(RegistroEmpresaId.class)
@Table(name = "tb_categoria_documento")
@EqualsAndHashCode(of = {"id", "chaveEmpresa"})
@NamedQueries({
        @NamedQuery(name = "CategoriaDocumento.findByEmpresa", query = "select ce from CategoriaDocumento ce where ce.empresa.chave = :empresa order by ce.nome"),
        @NamedQuery(name = "CategoriaDocumento.findUsadasByEmpresa", query = "select ce from Documento e inner join e.categoria ce where e.empresa.chave = :empresa group by ce order by ce.nome"),
        @NamedQuery(name = "CategoriaDocumento.findByEmpresaAndNome", query = "select ce from CategoriaDocumento ce where ce.empresa.chave = :empresa and lower(ce.nome) = :nome")
})
public class CategoriaDocumento implements IEntity  {

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_categoria_documento")
    @GeneratedValue(generator = "seq_categoria_documento", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(sequenceName = "seq_categoria_documento", name = "seq_categoria_documento")
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
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
}
