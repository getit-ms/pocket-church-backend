/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.util.string.StringUtil;
import java.util.Arrays;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Getter
@Entity
@NoArgsConstructor
@ToString(of = "chave")
@Table(name = "tb_dispositivo")
@EqualsAndHashCode(of = "chave")
@NamedQueries({
    @NamedQuery(name = "Dispositivo.findPorTipoAndIgreja", query = "select d.pushkey from Dispositivo d where d.igreja.chave = :igreja and d.tipo = :tipo and d.pushkey in :dispositivos"),
    @NamedQuery(name = "Dispositivo.desabilitaByPushkey", query = "update Dispositivo d set d.pushkey = 'unknown' where d.pushkey = :pushkey"),
    @NamedQuery(name = "Dispositivo.unregisterOldDevices", query = "update Dispositivo d set d.pushkey = 'unknown' where d.chave <> :chaveDispositivo and d.pushkey = :pushkey"),
    @NamedQuery(name = "Dispositivo.registerAcesso", query = "update Dispositivo d set d.ultimoAcesso = CURRENT_TIMESTAMP where d.chave in :chaves")
})
public class Dispositivo implements IEntity {
    @Id
    @Column(name = "chave", length = 250)
    private String chave;
    
    @NotEmpty
    @Length(max = 250)
    @Column(name = "uuid", nullable = false, length = 250)
    private String uuid;

    @Column(name = "ultimo_acesso")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimoAcesso = new Date();
    
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "tipo", nullable = false)
    private TipoDispositivo tipo = TipoDispositivo.UNKNOWN;
    
    @NotEmpty
    @Length(max = 250)
    @Column(name = "pushkey", nullable = false, length = 250)
    private String pushkey = "unknown";
    
    @NotEmpty
    @Length(max = 250)
    @Column(name = "versao", nullable = false, length = 30)
    private String versao = "unknown";
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja", nullable = false)
    private Igreja igreja;
    
    @Setter
    @OneToOne(mappedBy = "dispositivo")
    private Preferencias preferencias;
    
    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
        @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro membro;

    public Dispositivo(String uuid, Igreja igreja) {
        this.chave = uuid + "@" + igreja.getChave();
        this.uuid = uuid;
        this.igreja = igreja;
    }
    
    public void registerToken(TipoDispositivo td, String pushToken, String versao){
        if (td != null){
            this.tipo = td;
        }
        
        if (!StringUtil.isEmpty(pushToken)){
            this.pushkey = pushToken;
        }
        
        if (!StringUtil.isEmpty(versao)){
            this.versao = versao;
        }
    }
    
    @Override
    public String getId(){
        return chave;
    }

    public boolean isAdministrativo() {
        return TipoDispositivo.PC.equals(tipo);
    }

    public boolean isRegistrado() {
        return !"unknown".equals(pushkey);
    }
    
}
