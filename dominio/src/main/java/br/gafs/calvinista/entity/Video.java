package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.bean.OrderBy;
import br.gafs.calvinista.entity.domain.StatusItemEvento;
import br.gafs.calvinista.entity.domain.TipoItemEvento;
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
@EqualsAndHashCode(of = {"id", "igreja"})
@NamedQueries({
        @NamedQuery(name = "Video.findVideoNaoSincronizados", query = "select v from Video v where v.igreja.chave = :igreja and v.dataAtualizacao < :dataAtualizacao"),
        @NamedQuery(name = "Video.findByIgreja", query = "select v from Video v where v.igreja.chave = :igreja order by v.publicacao desc")
})
public class Video implements IEntity, IItemEvento {
    @Id
    @Column(name = "id_video")
    private String id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

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
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @JsonIgnore
    @Override
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId())
                .igreja(getIgreja())
                .tipo(TipoItemEvento.VIDEO)
                .titulo(getTitulo())
                .apresentacao(getDescricao())
                .status(StatusItemEvento.PUBLICADO)
                .dataHoraPublicacao(getAgendamento() != null ? getAgendamento() : getPublicacao())
                .dataHoraReferencia(getAgendamento() != null ? getAgendamento() : getPublicacao())
                .urlIlustracao(getThumbnail())
                .build();
    }

    public boolean isAgendado() {
        return agendamento != null && System.currentTimeMillis() < agendamento.getTime();
    }

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;

        if (igreja != null) {
            this.chaveIgreja = igreja.getChave();
        } else {
            this.chaveIgreja = null;
        }
    }
}
