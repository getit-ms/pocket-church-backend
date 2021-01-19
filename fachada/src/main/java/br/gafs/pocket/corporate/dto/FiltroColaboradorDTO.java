/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.dto.DTO;
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
public class FiltroColaboradorDTO implements DTO {
    private String nome;
    private String email;
    private String filtro;
    private boolean acessoRecente;
    private Integer pagina = 1;
    private Integer total = 10;
    private List<Long> perfis;
}
