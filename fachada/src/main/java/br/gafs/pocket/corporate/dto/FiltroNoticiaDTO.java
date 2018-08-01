/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
import br.gafs.pocket.corporate.entity.domain.TipoNoticia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroNoticiaDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    private TipoNoticia tipo;
    private Integer pagina = 1;
    private Integer total = 10;
}
