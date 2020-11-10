package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.FormatoCampoEvento;
import br.gafs.calvinista.entity.domain.TipoCampoEvento;
import br.gafs.calvinista.entity.domain.TipoValidacaoCampo;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_campo_evento")
public class CampoEvento implements IEntity, Comparable<CampoEvento> {

    @Id
    @Column(name = "id_campo_evento")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_campo_evento")
    @SequenceGenerator(sequenceName = "seq_campo_evento", name = "seq_campo_evento")
    private Long id;

    @NotEmpty
    @Length(max = 150)
    @Column(name = "nome")
    private String nome;

    @NotNull
    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    private TipoCampoEvento tipo;

    @NotNull
    @Column(name = "formato")
    @Enumerated(EnumType.ORDINAL)
    private FormatoCampoEvento formato;

    @Setter
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name = "id_evento", referencedColumnName = "id_evento"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja"),
    })
    private Evento evento;

    @ElementCollection
    @Column(name = "opcao")
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @CollectionTable(name = "tb_opcoes_campo_evento",
            joinColumns = @JoinColumn(name = "id_campo_evento", referencedColumnName = "id_campo_evento")
    )
    private List<String> opcoes = new ArrayList<>();

    @ElementCollection
    @Column(name = "valor")
    @JsonView(View.Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @MapKeyColumn(name = "tipo")
    @MapKeyEnumerated(EnumType.ORDINAL)
    @CollectionTable(name = "tb_validacao_campo_evento",
            joinColumns = @JoinColumn(name = "id_campo_evento", referencedColumnName = "id_campo_evento")
    )
    private Map<TipoValidacaoCampo, String> validacao = new HashMap<>();

    @Override
    public int compareTo(CampoEvento o) {
        return getId().compareTo(o.getId());
    }
}
