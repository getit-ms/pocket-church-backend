package br.gafs.calvinista.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@IdClass(AceiteTermoMembroId.class)
@Table(name = "rl_termo_aceite_membro")
@EqualsAndHashCode(of = {"idMembro", "idTermoAceite", "chaveIgreja"})
@NamedQueries({
        @NamedQuery(name = "AceiteTermoMembro.findByIgrejaAndMembro", query = "select atm from AceiteTermoMembro atm where atm.igreja.chave = :igreja and atm.membro.id = :membro order by atm.termoAceite.versao desc")
})
public class AceiteTermoMembro implements Serializable {
    @Id
    @Column(name = "id_membro", insertable = false, updatable = false)
    private Long idMembro;

    @Id
    @Column(name = "id_termo_aceite", insertable = false, updatable = false)
    private Long idTermoAceite;

    @Id
    @Column(name = "chave_igreja", insertable = false, updatable = false)
    private String chaveIgreja;

    @Column(name = "data_aceite")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataAceite = new Date();

    @Column(name = "chave_dispositivo")
    private String dispositivo;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_membro", referencedColumnName = "id_membro"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private Membro membro;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "id_termo_aceite", referencedColumnName = "id_termo_aceite"),
            @JoinColumn(name = "chave_igreja", referencedColumnName = "chave_igreja", insertable = false, updatable = false)
    })
    private TermoAceite termoAceite;

    @ManyToOne
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public AceiteTermoMembro(Membro membro, TermoAceite termoAceite, String dispositivo) {
        this.idMembro = membro.getId();
        this.membro = membro;
        this.chaveIgreja = membro.getChaveIgreja();
        this.igreja = membro.getIgreja();
        this.idTermoAceite = termoAceite.getId();
        this.termoAceite = termoAceite;
        this.dispositivo = dispositivo;
    }

    public AceiteTermoMembroId getIdMembro() {
        return new AceiteTermoMembroId(idMembro, idTermoAceite, chaveIgreja);
    }
}
