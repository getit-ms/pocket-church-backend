/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.StatusBoletim;
import br.gafs.calvinista.entity.domain.TipoBoletim;
import br.gafs.calvinista.view.View;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
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
@IdClass(RegistroIgrejaId.class)
@NamedQueries({
    @NamedQuery(name = "Boletim.findIgrejaByStatusAndDataPublicacao", query = "select i from Boletim b inner join b.igreja i where i.status = :statusIgreja and b.status = :statusBoletim and b.dataPublicacao <= :data and b.tipo = :tipo and b.divulgado = false group by i"),
    @NamedQuery(name = "Boletim.findUltimoADivulgar", query = "select b from Boletim b inner join b.igreja i where i.chave = :igreja and b.status = :statusBoletim and b.dataPublicacao <= :data and b.tipo = :tipo and b.divulgado = false order by b.dataPublicacao desc"),
    @NamedQuery(name = "Boletim.updateNaoDivulgadosByIgreja", query = "update Boletim b set b.divulgado = true where b.dataPublicacao <= :data and b.igreja.chave = :igreja and b.tipo = :tipo"),
    @NamedQuery(name = "Boletim.findByStatus", query = "select b from Boletim b where b.status = :status order by b.dataPublicacao"),
    @NamedQuery(name = "Boletim.updateStatus", query = "update Boletim b set b.status = :status where b.id = :boletim and b.igreja.chave = :igreja")
})
public class Boletim implements ArquivoPDF {

    private static final Map<RegistroIgrejaId, Integer> locks = new HashMap<RegistroIgrejaId, Integer>();
    
    public synchronized static boolean locked(RegistroIgrejaId id){
        return locks.containsKey(id);
    }
    
    public synchronized static int lock(RegistroIgrejaId id){
        return locks.get(id);
    }
    
    public synchronized static void lock(RegistroIgrejaId id, int percent){
        locks.put(id, percent);
    }
    
    public synchronized static void unlock(RegistroIgrejaId id){
        locks.remove(id);
    }
    
    public synchronized static int locked(){
        return locks.size();
    }
    
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_boletim")
    @SequenceGenerator(name = "seq_boletim", sequenceName = "seq_boletim")
    @GeneratedValue(generator = "seq_boletim", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "titulo")
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    private String titulo;

    @Column(name = "data")
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @View.MergeViews(View.Edicao.class)
    private Date data;

    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    @View.MergeViews(View.Edicao.class)
    private Date dataPublicacao;

    @Setter
    @JsonView(Resumido.class)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_alteracao")
    private Date ultimaAlteracao = DateUtil.getDataAtual();

    @Setter
    @NotNull
    @Column(name = "tipo")
    @JsonView(Resumido.class)
    @Enumerated(EnumType.ORDINAL)
    private TipoBoletim tipo = TipoBoletim.BOLETIM;

    @JsonView(Detalhado.class)
    @Column(name = "divulgado", nullable = false)
    private boolean divulgado;

    @JsonView(Detalhado.class)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusBoletim status = StatusBoletim.PROCESSANDO;

    @NotNull
    @OneToOne
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
        @JoinColumn(name = "id_arquivo", referencedColumnName = "id_arquivo", nullable = false),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo boletim;

    @Setter
    @OneToOne
    @JsonView(Resumido.class)
    @JoinColumns({
        @JoinColumn(name = "id_thumbnail", referencedColumnName = "id_arquivo"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Arquivo thumbnail;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "rl_boletim_paginas", joinColumns = {
        @JoinColumn(name = "id_boletim", referencedColumnName = "id_boletim", nullable = false),
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

    public Boletim(Igreja igreja) {
        this.igreja = igreja;
    }

    public void processando() {
        this.status = StatusBoletim.PROCESSANDO;
    }

    public boolean isAgendado() {
        return StatusBoletim.PUBLICADO.equals(status)
                && DateUtil.getDataAtual().before(dataPublicacao);
    }

    public boolean isPublicado() {
        return StatusBoletim.PUBLICADO.equals(status)
                && DateUtil.getDataAtual().after(dataPublicacao);
    }

    public boolean isProcessando() {
        return StatusBoletim.PROCESSANDO.equals(status);
    }

    public boolean isRejeitado() {
        return StatusBoletim.REJEITADO.equals(status);
    }
    
    public double getPorcentagemProcessamento(){
        RegistroIgrejaId riid = new RegistroIgrejaId(chaveIgreja, id);
        if (isProcessando() && locked(riid)){
            return lock(riid);
        }
        
        if (isPublicado() || isAgendado()){
            return 1d;
        }
        
        return 0d;
    }

    @JsonProperty
    @JsonView(Resumido.class)
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
