/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.pocket.corporate.entity.domain.StatusBoletimInformativo;
import br.gafs.pocket.corporate.entity.domain.StatusItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
import br.gafs.pocket.corporate.view.View;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_boletim")
@EqualsAndHashCode(of = "id")
@IdClass(RegistroEmpresaId.class)
@NamedQueries({
    @NamedQuery(name = "BoletimInformativo.findEmpresaByStatusAndDataPublicacao", query = "select i from BoletimInformativo b inner join b.empresa i where i.status = :statusEmpresa and b.status = :statusBoletim and b.dataPublicacao <= :data and b.tipo = :tipo and b.divulgado = false group by i"),
    @NamedQuery(name = "BoletimInformativo.findUltimoADivulgar", query = "select b from BoletimInformativo b inner join b.empresa e where e.chave = :empresa and b.status = :statusBoletim and b.dataPublicacao <= :data and b.tipo = :tipo and b.divulgado = false order by b.dataPublicacao desc"),
    @NamedQuery(name = "BoletimInformativo.updateNaoDivulgadosByEmpresa", query = "update BoletimInformativo b set b.divulgado = true where b.dataPublicacao <= :data and b.empresa.chave = :empresa and b.tipo = :tipo"),
    @NamedQuery(name = "BoletimInformativo.findByStatus", query = "select b from BoletimInformativo b where b.status = :status order by b.dataPublicacao"),
    @NamedQuery(name = "BoletimInformativo.updateStatus", query = "update BoletimInformativo b set b.status = :status where b.id = :boletim and b.empresa.chave = :empresa")
})
public class BoletimInformativo implements ArquivoPDF, IItemEvento {

    @Id
    @JsonView(View.Resumido.class)
    @Column(name = "id_boletim")
    @SequenceGenerator(name = "seq_boletim", sequenceName = "seq_boletim")
    @GeneratedValue(generator = "seq_boletim", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "titulo")
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    private String titulo;

    @Column(name = "data")
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    private Date data;

    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    private Date dataPublicacao;

    @Setter
    @JsonView(View.Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_alteracao")
    private Date ultimaAlteracao = DateUtil.getDataAtual();

    @Setter
    @NotNull
    @Column(name = "tipo")
    @JsonView(View.Resumido.class)
    @Enumerated(EnumType.ORDINAL)
    private TipoBoletimInformativo tipo = TipoBoletimInformativo.BOLETIM;

    @JsonView(View.Detalhado.class)
    @Column(name = "divulgado", nullable = false)
    private boolean divulgado;

    @JsonView(View.Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusBoletimInformativo status = StatusBoletimInformativo.PROCESSANDO;

    @NotNull
    @OneToOne
    @JsonView(View.Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
        @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo boletim;

    @Setter
    @OneToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
        @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo"),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "rl_boletim_paginas", joinColumns = {
        @JoinColumn(name = "id_boletim", referencedColumnName = "id_boletim", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
    }, inverseJoinColumns = {
        @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
        @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private List<Arquivo> paginas = new ArrayList<Arquivo>();

    @ManyToOne
    @JsonView(View.Resumido.class)
    @JoinColumns({
            @JoinColumn(name = "id_autor", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Colaborador autor;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;

    public BoletimInformativo(Empresa empresa) {
        this.empresa = empresa;
    }

    public void processando() {
        this.status = StatusBoletimInformativo.PROCESSANDO;
    }

    public boolean isAgendado() {
        return StatusBoletimInformativo.PUBLICADO.equals(status)
                && DateUtil.getDataAtual().before(dataPublicacao);
    }

    public boolean isPublicado() {
        return StatusBoletimInformativo.PUBLICADO.equals(status)
                && DateUtil.getDataAtual().after(dataPublicacao);
    }

    public boolean isProcessando() {
        return StatusBoletimInformativo.PROCESSANDO.equals(status);
    }

    public boolean isRejeitado() {
        return StatusBoletimInformativo.REJEITADO.equals(status);
    }
    
    public double getPorcentagemProcessamento(){
        if (isPublicado() || isAgendado()){
            return 1d;
        }
        
        return 0d;
    }

    @JsonProperty
    @JsonView(View.Resumido.class)
    public List<Arquivo> getPaginas() {
        Collections.sort(paginas);
        return paginas;
    }

    @Override
    @JsonIgnore
    public Arquivo getPDF() {
        return boletim;
    }

    @Override
    @JsonIgnore
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId().toString())
                .empresa(getEmpresa())
                .tipo(TipoItemEvento.BOLETIM_INFORMATIVO)
                .titulo(getTitulo())
                .dataHoraPublicacao(getDataPublicacao())
                .dataHoraReferencia(getDataPublicacao())
                .ilustracao(getThumbnail())
                .autor(getAutor())
                .status(
                        isPublicado() ?
                                StatusItemEvento.PUBLICADO :
                                StatusItemEvento.NAO_PUBLICADO
                )
                .build();
    }
}
