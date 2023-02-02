package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.TipoVersao;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"versao", "tipo"})
@EqualsAndHashCode(of = {"versao", "tipo"})
public class ReleaseNotesId implements Serializable {
    private String versao;
    private TipoVersao tipo;
}
