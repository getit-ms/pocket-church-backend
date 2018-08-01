/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.pocket.corporate.entity.domain.StatusBoletimInformativo;
import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @NamedQuery(name = "Boletim.findEmpresaByStatusAndDataPublicacao", query = "select i from Boletim b inner join b.empresa i where i.status = :statusEmpresa and b.status = :statusBoletim and b.dataPublicacao <= :data and b.tipo = :tipo and b.divulgado = false group by i"),
    @NamedQuery(name = "Boletim.updateNaoDivulgadosByEmpresa", query = "update Boletim b set b.divulgado = true where b.dataPublicacao <= :data and b.empresa.chave = :empresa and b.tipo = :tipo"),
    @NamedQuery(name = "Boletim.findByStatus", query = "select b from Boletim b where b.status = :status order by b.dataPublicacao"),
    @NamedQuery(name = "Boletim.updateStatus", query = "update Boletim b set b.status = :status where b.id = :boletim and b.empresa.chave = :empresa")
})
public class BoletimInformativo implements ArquivoPDF {

    private static final Map<RegistroEmpresaId, Integer> locks = new HashMap<RegistroEmpresaId, Integer>();
    
    public synchronized static boolean locked(RegistroEmpresaId id){
        return locks.containsKey(id);
    }
    
    public synchronized static int lock(RegistroEmpresaId id){
        return locks.get(id);
    }
    
    public synchronized static void lock(RegistroEmpresaId id, int percent){
        locks.put(id, percent);
    }
    
    public synchronized static void unlock(RegistroEmpresaId id){
        locks.remove(id);
    }
    
    public synchronized static int locked(){
        return locks.size();
    }
    
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
        RegistroEmpresaId riid = new RegistroEmpresaId(chaveEmpresa, id);
        if (isProcessando() && locked(riid)){
            return lock(riid);
        }
        
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
}
