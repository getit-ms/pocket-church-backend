/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.dto.DTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@AllArgsConstructor
public class FiltroEventoDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    private Integer pagina = 1;
    private Integer total = 10;
}
