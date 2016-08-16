package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.TipoVersao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@ToString(of = "versao")
@IdClass(ReleaseNotesId.class)
@EqualsAndHashCode(of = "versao")
@Table(name = "tb_release_notes")
@NamedQueries({
    @NamedQuery(name = "ReleaseNotes.findByTipo", query = "select rn from ReleaseNotes rn where rn.tipo = :tipo order rn.data desc")
})
public class ReleaseNotes implements IEntity {
    @Id
    @Setter
    @Column(name = "versao")
    private String versao;
    @Id
    @Setter
    @JsonIgnore
    @Column(name = "tipo")
    @Enumerated(EnumType.ORDINAL)
    private TipoVersao tipo;
    @Setter
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    @Setter
    @Column(name = "data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data = new Date();

    @Override
    @JsonIgnore
    public ReleaseNotesId getId() {
        return new ReleaseNotesId(versao, tipo);
    }
}
