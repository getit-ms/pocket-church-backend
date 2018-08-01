/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.dto.DTO;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ParametrosGlobaisDTO implements DTO {
    @TipoParametro.Mapping(TipoParametro.REPOSITORY_URL)
    private String repositoryURL;
}
