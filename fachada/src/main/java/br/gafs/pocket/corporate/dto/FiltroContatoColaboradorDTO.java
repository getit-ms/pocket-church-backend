/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.StatusContatoColaborador;
import br.gafs.dto.DTO;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
public class FiltroContatoColaboradorDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    private List<StatusContatoColaborador> status = Arrays.asList(StatusContatoColaborador.PENDENTE);
    private Integer pagina = 1;
    private Integer total = 10;
}
