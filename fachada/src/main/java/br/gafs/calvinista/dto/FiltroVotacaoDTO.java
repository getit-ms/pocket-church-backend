/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.dto.DTO;
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
public class FiltroVotacaoDTO implements DTO {
    private Date data;
    private String nome;
    private Integer pagina = 1;
    private Integer total = 10;
}
