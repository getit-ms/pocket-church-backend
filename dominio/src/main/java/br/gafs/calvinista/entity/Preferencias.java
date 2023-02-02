/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.HorasEnvioNotificacao;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel
 */
@Entity
@Getter
@ToString(of = "dispositivo")
@EqualsAndHashCode(of = "dispositivo")
@Table(name = "tb_preferencias")
@NoArgsConstructor(onConstructor = @_(@Deprecated))
@NamedQueries({
        @NamedQuery(name = "Preferencias.findByMembro", query = "select p from Preferencias p where p.dispositivo.membro.id = :membro and p.dispositivo.igreja.chave = :igreja")
})
public class Preferencias implements IEntity {
    @Id
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "chave_dispositivo")
    private Dispositivo dispositivo;

    @Setter
    @Enumerated(EnumType.ORDINAL)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "hora_versiculo_diario")
    private HorasEnvioNotificacao horaVersiculoDiario = HorasEnvioNotificacao._14_00;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_versiculos_diarios")
    private boolean desejaReceberVersiculosDiarios = true;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_notificacoes_videos")
    private boolean desejaReceberNotificacoesVideos = true;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_lembretes_leitura_biblica")
    private boolean desejaReceberLembreteLeitura = true;

    @Setter
    @Enumerated(EnumType.ORDINAL)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "hora_lembrete_leitura")
    private HorasEnvioNotificacao horaLembreteLeitura = HorasEnvioNotificacao._14_00;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_notificacoes_devocionario")
    private boolean desejaReceberNotificacoesDevocionario = true;

    @Setter
    @Enumerated(EnumType.ORDINAL)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "hora_notificacoes_devocionario")
    private HorasEnvioNotificacao horaNotificacoesDevocional = HorasEnvioNotificacao._14_00;

    @Setter
    @ManyToMany
    @View.MergeViews(View.Edicao.class)
    @JoinTable(name = "rl_preferencias_ministerios",
            joinColumns = @JoinColumn(name = "chave_dispositivo"),
            inverseJoinColumns = {
                    @JoinColumn(name = "id_ministerio", referencedColumnName = "id_ministerio"),
                    @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja")
            })
    private List<Ministerio> ministeriosInteresse = new ArrayList<Ministerio>();

    @Transient
    @JsonIgnore
    @View.MergeViews(View.Edicao.class)
    private Boolean dadosDisponiveis;

    public Preferencias(Dispositivo dispositivo) {
        this.dispositivo = dispositivo;
    }

    @JsonProperty
    public Boolean getDadosDisponiveis() {
        if (dadosDisponiveis != null) {
            return dadosDisponiveis;
        }

        if (dispositivo == null || dispositivo.getMembro() == null) {
            return null;
        }

        return dispositivo.getMembro().isDadosDisponiveis();
    }

    @JsonProperty
    public void setDadosDisponiveis(Boolean dadosDisponiveis) {
        if (dadosDisponiveis != null && !dadosDisponiveis.equals(getDadosDisponiveis())) {
            this.dadosDisponiveis = dadosDisponiveis;
        }
    }

    @Override
    @JsonIgnore
    public String getId() {
        return dispositivo.getChave();
    }

    public void copia(Preferencias outro) {
        outro.setDesejaReceberVersiculosDiarios(desejaReceberVersiculosDiarios);
        outro.setDesejaReceberLembreteLeitura(desejaReceberLembreteLeitura);
        outro.setDesejaReceberNotificacoesVideos(desejaReceberNotificacoesVideos);
        outro.setDesejaReceberNotificacoesDevocionario(desejaReceberNotificacoesDevocionario);
        outro.setDadosDisponiveis(dadosDisponiveis);
        outro.setMinisteriosInteresse(ministeriosInteresse);
        outro.setHoraVersiculoDiario(horaVersiculoDiario);
        outro.setHoraLembreteLeitura(horaLembreteLeitura);
        outro.setHoraNotificacoesDevocional(horaNotificacoesDevocional);
    }
}
