/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import br.gafs.pocket.corporate.entity.domain.StatusInscricaoEvento;
import br.gafs.pocket.corporate.entity.domain.TipoEvento;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Gabriel
 */
@Data
@AllArgsConstructor
public class FiltroInscricaoDTO implements DTO {
    private TipoEvento tipoEvento;
    private List<StatusInscricaoEvento> status;
    private Integer pagina;
    private Integer total;
}
