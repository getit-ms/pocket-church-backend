package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusItemEvento;
import br.gafs.calvinista.entity.domain.TipoAudio;
import br.gafs.calvinista.entity.domain.TipoItemEvento;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by Gabriel on 23/07/2018.
 */
@Getter
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_audio")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroIgrejaId.class)
public class Audio implements IEntity, IItemEvento {

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
    @Column(name = "autoria", length = 150)
    private String autoria;

    @Setter
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "descricao")
    private String descricao;

    @Column(name = "data_hora")
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = new Date();

    @Setter
    @NotNull
    @ManyToOne
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria_audio"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
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

    @Column(name = "data_cadastro")
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCadastro = new Date();

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_arquivo_capa", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo capa;

    @Setter
    @NotNull
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_arquivo_audio", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo audio;

    @Setter
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_autor", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false),
    })
    private Membro autor;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Override
    @JsonIgnore
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId().toString())
                .igreja(getIgreja())
                .tipo(TipoItemEvento.AUDIO)
                .titulo(getNome())
                .dataHoraPublicacao(getDataHora())
                .dataHoraReferencia(getDataHora())
                .ilustracao(getCapa())
                .autor(getAutor())
                .status(StatusItemEvento.PUBLICADO)
                .build();
    }
}
