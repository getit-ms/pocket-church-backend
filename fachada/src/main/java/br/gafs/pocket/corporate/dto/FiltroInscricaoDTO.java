/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
@AllArgsConstructor
public class FiltroInscricaoDTO implements DTO {
    private Integer pagina;
    private Integer total;
}
