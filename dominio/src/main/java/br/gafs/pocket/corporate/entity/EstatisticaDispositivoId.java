package br.gafs.pocket.corporate.entity;

import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"empresa", "data", "tipoDispositivo"})
public class EstatisticaDispositivoId implements Serializable {
    private String empresa;
    private Date data;
    private TipoDispositivo tipoDispositivo;
}
