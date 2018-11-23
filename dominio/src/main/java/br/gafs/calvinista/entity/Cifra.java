package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.StatusCifra;
import br.gafs.calvinista.entity.domain.TipoCifra;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_cifra")
@IdClass(RegistroIgrejaId.class)
@ToString(of = {"id", "igreja"})
@EqualsAndHashCode(of = {"id", "igreja"})
public class Cifra implements ArquivoPDF {
    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_cifra")
    @SequenceGenerator(name = "seq_cifra", sequenceName = "seq_cifra")
    @GeneratedValue(generator = "seq_cifra", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "letra", nullable = false, columnDefinition = "TEXT")
    private String letra;

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Resumido.class)
    private StatusCifra status = StatusCifra.PROCESSANDO;

    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    private TipoCifra tipo = TipoCifra.CIFRA;

    @NotNull
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo cifra;

    @Setter
    @NotNull
    @OneToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @ManyToMany
    @JsonView(View.Resumido.class)
    @JoinTable(name = "rl_cifra_paginas", joinColumns = {
            @JoinColumn(name = "id_cifra", referencedColumnName = "id_cifra", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
    }, inverseJoinColumns = {
            @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private List<Arquivo> paginas = new ArrayList<Arquivo>();

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;

    @Override
    @JsonIgnore
    public Arquivo getPDF() {
        return cifra;
    }

    public void processando() {
        status = StatusCifra.PROCESSANDO;
    }
}
