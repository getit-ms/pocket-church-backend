package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.StatusDiaDevocionario;
import br.gafs.calvinista.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
@Entity
@IdClass(RegistroIgrejaId.class)
@Table(name = "tb_dia_devocionario")
@EqualsAndHashCode(of = {"id", "chaveIgreja"})
@NamedQueries({
        @NamedQuery(name = "DiaDevocionario.findIgrejaByStatusAndDataPublicacao", query = "select i from DiaDevocionario dd inner join dd.igreja i where i.status = :statusIgreja and dd.status = :statusDiaDevocionario and dd.data = :data and dd.divulgado = false group by i"),
        @NamedQuery(name = "DiaDevocionario.findByIgrejaAndData", query = "select dd from DiaDevocionario dd inner join dd.igreja i where i.chave = :igreja and dd.data = :data"),
        @NamedQuery(name = "DiaDevocionario.updateNaoDivulgadosByIgreja", query = "update DiaDevocionario dd set dd.divulgado = true where dd.data = :data and dd.igreja.chave = :igreja"),
})
public class DiaDevocionario implements ArquivoPDF {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_dia_devocionario")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_dia_devocionario")
    @SequenceGenerator(sequenceName = "seq_dia_devocionario", name = "seq_dia_devocionario")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Column(name = "data")
    @Temporal(TemporalType.DATE)
    @JsonView(View.Resumido.class)
    @View.JsonTemporal(View.JsonTemporalType.DATE)
    private Date data;

    @Setter
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    @JsonView(View.Detalhado.class)
    @Column(name = "divulgado", nullable = false)
    private boolean divulgado;

    @JsonView(View.Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusDiaDevocionario status = StatusDiaDevocionario.PROCESSANDO;

    @Setter
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_alteracao")
    private Date ultimaAlteracao = DateUtil.getDataAtual();

    @Setter
    @NotNull
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo arquivo;

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @JsonIgnore
    @Deprecated
    public List<Arquivo> getPaginas() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public void setPaginas(List<Arquivo> arquivos) {
        // Não necessário
    }

    @Override
    @JsonIgnore
    public Arquivo getPDF() {
        return arquivo;
    }

    public void processando() {
        status = StatusDiaDevocionario.PROCESSANDO;
    }
}
