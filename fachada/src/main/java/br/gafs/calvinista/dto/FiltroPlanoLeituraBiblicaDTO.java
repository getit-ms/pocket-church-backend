/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
import java.util.Date;
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
public class FiltroPlanoLeituraBiblicaDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    private String descricao;
    private Integer pagina = 1;
    private Integer total = 10;
}
