/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.StatusInscricaoEvento;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.dto.DTO;
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
