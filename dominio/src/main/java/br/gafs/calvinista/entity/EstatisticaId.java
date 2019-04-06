package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.TipoDispositivo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"igreja", "data", "tipoDispositivo"})
public class EstatisticaId implements Serializable {
    private String igreja;
    private Date data;
    private TipoDispositivo tipoDispositivo;
}
