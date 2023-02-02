package br.gafs.calvinista.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Gabriel on 24/03/2018.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "idBiblia"})
public class LivroBibliaId implements Serializable {
    private Long id;
    private Long idBiblia;
}
