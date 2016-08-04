/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.TipoParametro;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"grupo", "chave"})
@EqualsAndHashCode(of = {"grupo", "chave"})
public class ParametroId implements Serializable {
    private String grupo;
    private TipoParametro chave;
}
