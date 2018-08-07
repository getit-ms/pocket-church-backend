package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.TipoAudio;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by Gabriel on 23/07/2018.
 */
@Getter
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_audio")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
public class Audio implements IEntity {

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_audio")
    @GeneratedValue(generator = "seq_audio", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(sequenceName = "seq_audio", name = "seq_audio")
    private Long id;

    @Setter
    @NotEmpty
    @Length(max = 150)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @Setter
    @Length(max = 150)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "autor", length = 150)
    private String autor;

    @Setter
    @NotNull
    @ManyToOne
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria_audio"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private CategoriaAudio categoria;

    @Setter
    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Resumido.class)
    private TipoAudio tipo;

    @Setter
    @Column(name = "id_externo")
    @JsonView(View.Resumido.class)
    private String idExterno;

    @Setter
    @Column(name = "url_stream")
    @JsonView(View.Resumido.class)
    private String urlStream;

    @Setter
    @Column(name = "link_externo")
    @JsonView(View.Resumido.class)
    private String linkExterno;

    @Setter
    @JsonView(View.Resumido.class)
    @Column(name = "tamanho_arquivo")
    private long tamamnhoArquivo;

    @Setter
    @JsonView(View.Resumido.class)
    @Column(name = "tempo_audio")
    private long tempoAudio;

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_arquivo_capa", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo capa;

    @Setter
    @NotNull
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_arquivo_audio", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo audio;

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
