/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.HorasEnvioNotificacao;
import br.gafs.pocket.corporate.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Entity
@Getter
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "tb_preferencias")
@NoArgsConstructor(onConstructor = @_(@Deprecated))
@NamedQueries({
    @NamedQuery(name = "Preferencias.findByColaborador", query = "select p from Preferencias p where p.dispositivo.colaborador.id = :colaborador and p.dispositivo.empresa.chave = :empresa")
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
    @Column(name = "hora_mensagem_dia")
    private HorasEnvioNotificacao horaMensagemDia = HorasEnvioNotificacao._14_00;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_mensagens_dia")
    private boolean desejaReceberMensagensDia = true;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_notificacoes_videos")
    private boolean desejaReceberNotificacoesVideos = true;

    @Transient
    @JsonIgnore
    @View.MergeViews(View.Edicao.class)
    private Boolean dadosDisponiveis;
    
    public Preferencias(Dispositivo dispositivo) {
        this.dispositivo = dispositivo;
    }
    
    @JsonProperty
    public Boolean getDadosDisponiveis(){
        if (dadosDisponiveis != null){
            return dadosDisponiveis;
        }
        
        if (dispositivo == null || dispositivo.getColaborador() == null){
            return null;
        }
        
        return dispositivo.getColaborador().isDadosDisponiveis();
    }
    
    @JsonProperty
    public void setDadosDisponiveis(Boolean dadosDisponiveis){
        if (dadosDisponiveis != null && !dadosDisponiveis.equals(getDadosDisponiveis())){
            this.dadosDisponiveis = dadosDisponiveis;
        }
    }
    
    @Override
    @JsonIgnore
    public String getId() {
        return dispositivo.getChave();
    }

    public void copia(Preferencias outro){
        outro.setDesejaReceberMensagensDia(desejaReceberMensagensDia);
        outro.setDesejaReceberNotificacoesVideos(desejaReceberNotificacoesVideos);
        outro.setDadosDisponiveis(dadosDisponiveis);
        outro.setHoraMensagemDia(horaMensagemDia);
    }
}
