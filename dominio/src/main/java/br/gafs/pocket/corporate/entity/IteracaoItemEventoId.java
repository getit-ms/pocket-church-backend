package br.gafs.pocket.corporate.entity;

import br.gafs.pocket.corporate.entity.domain.TipoItemEvento;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idColaborador", "idItemEvento", "chaveEmpresa", "tipoItemEvento"})
public class IteracaoItemEventoId implements Serializable {
    private Long idColaborador;

    private String idItemEvento;

    private String chaveEmpresa;

    private TipoItemEvento tipoItemEvento;
}
