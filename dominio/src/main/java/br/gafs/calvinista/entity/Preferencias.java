/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.HorasEnvioVersiculo;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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
    private HorasEnvioVersiculo horaVersiculoDiario = HorasEnvioVersiculo._14_00;

    @Setter
    @View.MergeViews(View.Edicao.class)
    @Column(name = "deseja_receber_versiculos_diarios")
    private boolean desejaReceberVersiculosDiarios = true;
    
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
    
    public Preferencias(Dispositivo dispositivo) {
        this.dispositivo = dispositivo;
    }

    @Override
    public String getId() {
        return dispositivo.getChave();
    }

    public void copia(Preferencias outro){
        outro.setDesejaReceberVersiculosDiarios(desejaReceberVersiculosDiarios);
        outro.setMinisteriosInteresse(ministeriosInteresse);
        outro.setHoraVersiculoDiario(horaVersiculoDiario);
    }
}
