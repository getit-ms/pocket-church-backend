package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.bean.OrderBy;
import br.gafs.pocket.corporate.entity.domain.StatusItemEvento;
import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_video")
@IdClass(VideoId.class)
@EqualsAndHashCode(of = {"id", "empresa"})
@NamedQueries({
        @NamedQuery(name = "Video.findVideoNaoSincronizados", query = "select v from Video v where v.empresa.chave = :empresa and v.dataAtualizacao < :dataAtualizacao"),
        @NamedQuery(name = "Video.findByEmpresa", query = "select v from Video v where v.empresa.chave = :empresa order by v.publicacao desc")
})
public class Video implements IEntity, IItemEvento {
    @Id
    @Column(name = "id_video")
    private String id;

    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Setter
    @Column(name = "thumbnail")
    private String thumbnail;

    @OrderBy
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_publicacao")
    private Date publicacao;

    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_agendamento")
    private Date agendamento;

    @Setter
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_atualizacao")
    private Date dataAtualizacao;

    @Column(name = "ao_vivo")
    private boolean aoVivo;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    @JsonIgnore
    @Override
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId())
                .empresa(getEmpresa())
                .tipo(TipoItemEvento.VIDEO)
                .titulo(getTitulo())
                .status(StatusItemEvento.PUBLICADO)
                .dataHoraPublicacao(getAgendamento() != null ? getAgendamento() : getPublicacao())
                .dataHoraReferencia(getAgendamento() != null ? getAgendamento() : getPublicacao())
                .urlIlustracao(getThumbnail())
                .build();
    }

    public boolean isAgendado() {
        return agendamento != null && System.currentTimeMillis() < agendamento.getTime();
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;

        if (empresa != null) {
            this.chaveEmpresa = empresa.getChave();
        } else {
            this.chaveEmpresa = null;
        }
    }
}
