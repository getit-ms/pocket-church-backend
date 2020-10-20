package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoNoticia;
import br.gafs.pocket.corporate.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by Gabriel on 11/03/2018.
 */
@Getter
@Entity
@Table(name = "tb_noticia")
@IdClass(RegistroEmpresaId.class)
@EqualsAndHashCode(of = {"empresa", "id"})
@NamedQueries({
        @NamedQuery(name = "Noticia.findEmpresaNaoDivultadosByDataPublicacao", query = "select i from Noticia n inner join n.empresa i where i.status = :statusEmpresa and n.divulgado = false and n.dataPublicacao <= :data and n.tipo = :tipoNoticia group by i"),
        @NamedQuery(name = "Noticia.updateNaoDivulgadosByEmpresa", query = "update Noticia n set n.divulgado = true where n.empresa.chave = :empresa and n.dataPublicacao <= :data and n.tipo = :tipoNoticia"),
        @NamedQuery(name = "Noticia.findUltimaADivulgar", query = "select n from Noticia n where n.empresa.chave = :empresa and n.dataPublicacao <= :data and n.tipo = :tipoNoticia and n.divulgado = false order by n.dataPublicacao desc"),
})
public class Noticia implements IEntity, IItemEvento {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_noticia")
    @SequenceGenerator(name = "seq_noticia", sequenceName = "seq_noticia")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_noticia")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @Setter
    @NotNull
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    @View.JsonTemporal(View.JsonTemporalType.TIMESTAMP)
    private Date dataPublicacao;

    @NotNull
    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Resumido.class)
    private TipoNoticia tipo = TipoNoticia.NOTICIA;

    @Setter
    @NotEmpty
    @Length(max = 150)
    @Column(name = "titulo")
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    private String titulo;

    @Setter
    @Column(name = "divulgado")
    @JsonView(View.Detalhado.class)
    private boolean divulgado = false;

    @Setter
    @NotEmpty
    @Column(name = "texto")
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    private String texto;

    @Setter
    @Column(name = "resumo")
    @JsonView(View.Resumido.class)
    private String resumo;

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_ilustracao", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo ilustracao;

    @Setter
    @ManyToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_autor", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Colaborador autor;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;

    @Override
    @JsonIgnore
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId().toString())
                .empresa(getEmpresa())
                .tipo(TipoItemEvento.NOTICIA)
                .titulo(getTitulo())
                .dataHora(getDataPublicacao())
                .ilustracao(getIlustracao())
                .status(
                        isPublicado() ?
                                StatusItemEvento.PUBLICADO :
                                StatusItemEvento.NAO_PUBLICADO
                )
                .build();
    }

    private boolean isPublicado() {
        return DateUtil.getDataAtual().after(dataPublicacao);
    }
}
