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
@EqualsAndHashCode(of = {"id", "chaveEmpresa", "tipo"})
public class ItemEventoId implements Serializable {
    private String id;
    private String chaveEmpresa;
    private TipoItemEvento tipo;
}
