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
@Table(name = "tb_lotacao_colaborador")
@EqualsAndHashCode(of = {"id", "chaveEmpresa"})
@NamedQueries({
        @NamedQuery(name = "LotacaoColaborador.findByEmpresa", query = "select lc from LotacaoColaborador lc where lc.empresa.chave = :empresa order by lc.nome"),
        @NamedQuery(name = "LotacaoColaborador.findUsadasByEmpresa", query = "select lc from Colaborador c inner join c.lotacao lc where c.empresa.chave = :empresa group by lc order by lc.nome"),
        @NamedQuery(name = "LotacaoColaborador.findByEmpresaAndNome", query = "select lc from LotacaoColaborador lc where lc.empresa.chave = :empresa and lower(lc.nome) = :nome")
})
public class LotacaoColaborador implements IEntity {

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_lotacao_colaborador")
    @GeneratedValue(generator = "seq_lotacao_colaborador", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(sequenceName = "seq_lotacao_colaborador", name = "seq_lotacao_colaborador")
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
