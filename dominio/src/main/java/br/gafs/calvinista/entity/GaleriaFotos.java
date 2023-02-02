package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusItemEvento;
import br.gafs.calvinista.entity.domain.TipoItemEvento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tb_galeria_fotos")
@IdClass(GaleriaFotosId.class)
@EqualsAndHashCode(of = {"id", "igreja"})
@NamedQueries({
        @NamedQuery(name = "GaleriaFotos.findNaoSincronizados", query = "select gf from GaleriaFotos gf where gf.igreja.chave = :igreja and gf.sincronizacao < :sincronizacao"),
        @NamedQuery(name = "GaleriaFotos.findByIgreja", query = "select gf from GaleriaFotos gf where gf.igreja.chave = :igreja order by gf.dataAtualizacao desc"),
        @NamedQuery(name = "GaleriaFotos.countByIgreja", query = "select count(gf.id) from GaleriaFotos gf where gf.igreja.chave = :igreja")
})
public class GaleriaFotos implements IEntity, IItemEvento {
    private static final ObjectMapper OM = new ObjectMapper();

    @Id
    @Column(name = "id_galeria_fotos")
    private String id;

    @Id
    @JsonIgnore
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Column(name = "nome")
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_atualizacao")
    private Date dataAtualizacao;

    @Setter
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_sincronizacao")
    private Date sincronizacao;

    @JsonIgnore
    @Column(name = "foto_primaria")
    private String fotoPrimaria;

    @Column(name = "quantidade_fotos")
    private Integer quantidadeFotos;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    @Builder
    public GaleriaFotos(String id, String nome, String descricao, Date dataAtualizacao, Foto fotoPrimaria, Integer quantidadeFotos) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataAtualizacao = dataAtualizacao;
        this.quantidadeFotos = quantidadeFotos;
        setFotoPrimaria(fotoPrimaria);
    }

    public void setIgreja(Igreja igreja) {
        this.igreja = igreja;
        if (igreja != null) {
            this.chaveIgreja = igreja.getChave();
        } else {
            this.chaveIgreja = null;
        }
    }

    @JsonIgnore
    @Override
    public ItemEvento getItemEvento() {
        return ItemEvento.builder()
                .id(getId())
                .igreja(getIgreja())
                .tipo(TipoItemEvento.FOTOS)
                .titulo(getNome())
                .status(StatusItemEvento.PUBLICADO)
                .dataHoraPublicacao(getDataAtualizacao())
                .dataHoraReferencia(getDataAtualizacao())
                .urlIlustracao(getFotoPrimaria().toString())
                .build();
    }

    @JsonProperty
    public Foto getFotoPrimaria() {
        if (fotoPrimaria != null) {
            try {
                return OM.readValue(fotoPrimaria, Foto.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void setFotoPrimaria(Foto fotoPrimaria) {
        if (fotoPrimaria != null) {
            try {
                this.fotoPrimaria = OM.writeValueAsString(fotoPrimaria);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            this.fotoPrimaria = null;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Foto implements Serializable {
        private String id;
        private String server;
        private String farm;
        private String secret;
        private String titulo;

        @Override
        public String toString() {
            return "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_n.jpg";
        }
    }
}
