package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusItemEvento;
import br.gafs.calvinista.entity.domain.TipoItemEvento;
import br.gafs.calvinista.view.View;
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
@IdClass(RegistroIgrejaId.class)
@EqualsAndHashCode(of = {"igreja", "id"})
@NamedQueries({
        @NamedQuery(name = "Noticia.findIgrejaNaoDivultadosByDataPublicacao", query = "select i from Noticia n inner join n.igreja i where i.status = :statusIgreja and n.divulgado = false and n.dataPublicacao <= :data group by i"),
        @NamedQuery(name = "Noticia.updateNaoDivulgadosByIgreja", query = "update Noticia n set n.divulgado = true where n.igreja.chave = :igreja and n.dataPublicacao <= :data"),
        @NamedQuery(name = "Noticia.findUltimaADivulgar", query = "select n from Noticia n where n.igreja.chave = :igreja and n.dataPublicacao <= :data and n.divulgado = false order by n.dataPublicacao desc"),
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
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Setter
    @NotNull
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    @View.JsonTemporal(View.JsonTemporalType.TIMESTAMP)
    private Date dataPublicacao;

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
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo ilustracao;

    @Setter
    @ManyToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_autor", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro autor;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    private boolean isPublicado() {
        return DateUtil.getDataAtual().after(dataPublicacao);
    }

    @Override
    @JsonIgnore
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId().toString())
                .igreja(getIgreja())
                .tipo(TipoItemEvento.NOTICIA)
                .autor(autor)
                .apresentacao(getResumo())
                .titulo(getTitulo())
                .dataHoraPublicacao(getDataPublicacao())
                .dataHoraReferencia(getDataPublicacao())
                .ilustracao(getIlustracao())
                .status(
                        isPublicado() ?
                                StatusItemEvento.PUBLICADO :
                                StatusItemEvento.NAO_PUBLICADO
                )
                .build();
    }

}
