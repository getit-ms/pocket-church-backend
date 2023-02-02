/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@NoArgsConstructor
public class FiltroMeusPedidoOracaoDTO extends FiltroPedidoOracaoDTO {

    public FiltroMeusPedidoOracaoDTO(Integer pagina, Integer total) {
        super(null, null, null, pagina, total);
    }
    
}
