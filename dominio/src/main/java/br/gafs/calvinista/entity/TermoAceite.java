package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_termo_aceite")
@EqualsAndHashCode(of = {"id", "chaveIgreja"})
@NamedQueries({
        @NamedQuery(name = "TermoAceite.findByIgreja", query = "select ta from TermoAceite ta where ta.igreja.chave = :igreja order by ta.versao desc")
})
public class TermoAceite implements IEntity {
    @Id
    @JsonIgnore
    @Column(name = "id_termo_aceite")
    @SequenceGenerator(name = "seq_termo_aceite", sequenceName = "seq_termo_aceite")
    @GeneratedValue(generator = "seq_termo_aceite", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Setter
    @JsonView(View.Resumido.class)
    @Column(name = "versao")
    private Integer versao = 0;

    @Column(name = "termo")
    @View.MergeViews(View.Edicao.class)
    @JsonView(View.Resumido.class)
    private String termo;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;
        this.chaveIgreja = igreja.getChave();
    }
}
