/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
import br.gafs.dto.DTO;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroBoletimDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    @JsonIgnore
    private TipoBoletimInformativo tipo;
    private Integer pagina = 1;
    private Integer total = 10;
}
