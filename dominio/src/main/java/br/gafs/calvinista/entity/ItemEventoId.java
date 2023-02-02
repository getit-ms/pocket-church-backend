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
@EqualsAndHashCode(of = {"id", "chaveIgreja", "tipo"})
public class ItemEventoId implements Serializable {
    private String id;
    private String chaveIgreja;
    private TipoItemEvento tipo;
}
