package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.TipoItemEvento;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idMembro", "idItemEvento", "chaveIgreja", "tipoItemEvento"})
public class IteracaoItemEventoId implements Serializable {
    private Long idMembro;

    private String idItemEvento;

    private String chaveIgreja;

    private TipoItemEvento tipoItemEvento;
}
