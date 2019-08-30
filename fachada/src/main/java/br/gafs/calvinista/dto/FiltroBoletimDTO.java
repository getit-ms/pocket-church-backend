/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoBoletim;
import br.gafs.dto.DTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class FiltroBoletimDTO implements DTO {
    private String filtro;
    private Date dataInicio;
    private Date dataTermino;
    @JsonIgnore
    private TipoBoletim tipo;
    private Integer pagina = 1;
    private Integer total = 10;
}
