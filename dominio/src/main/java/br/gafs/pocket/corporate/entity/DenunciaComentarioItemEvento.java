package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusDenunciaComentarioItemEvento;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@IdClass(RegistroEmpresaId.class)
@EqualsAndHashCode(of = {"id", "empresa"})
@Table(name = "tb_denuncia_comentario_item_evento")
@NamedQueries({
        @NamedQuery(name = "DenunciaComentarioItemEvento.findByComentario", query = "select d from DenunciaComentarioItemEvento  d where d.empresa.chave = :empresa and d.status = :statusDenuncia and d.comentario.id = :comentario order by d.dataHoraDenuncia")
})
public class DenunciaComentarioItemEvento implements IEntity {
    @Id
    @Column(name = "id_denuncia_comentario_item_evento")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_denuncia_comentario_item_evento")
    @SequenceGenerator(name = "seq_denuncia_comentario_item_evento", sequenceName = "seq_denuncia_comentario_item_evento")
    private Long id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @Column(name = "data_hora_denuncia")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraDenuncia = new Date();

    @NotEmpty
    @Column(name = "justificativa")
    @View.MergeViews(View.Cadastro.class)
    private String justificativa;

    @Column(name = "data_hora_analise")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraAnalise;

    @Setter
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_denunciante", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Colaborador denunciante;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_analista", referencedColumnName = "id_colaborador"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private Colaborador analista;

    @JsonIgnore
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private StatusDenunciaComentarioItemEvento status = StatusDenunciaComentarioItemEvento.PENDENTE;

    @Setter
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "id_comentario_item_evento", referencedColumnName = "id_comentario_item_evento"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false),
    })
    private ComentarioItemEvento comentario;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
    private Empresa empresa;

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
        if (empresa != null) {
            this.chaveEmpresa = empresa.getChave();
        } else {
            this.chaveEmpresa = null;
        }
    }

    public void atende(Colaborador analista) {
        this.status = StatusDenunciaComentarioItemEvento.ATENDIDO;
        this.analista = analista;
        this.dataHoraAnalise = new Date();
        this.comentario.censurado();
    }

    public void rejeita(Colaborador analista) {
        this.status = StatusDenunciaComentarioItemEvento.REJEITADO;
        this.analista = analista;
        this.dataHoraAnalise = new Date();
    }
}
