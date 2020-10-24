/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoEvento;
import br.gafs.util.date.DateUtil;

/**
 *
 * @author Gabriel
 */
public class FiltroEventoFuturoDTO extends FiltroEventoDTO {

    public FiltroEventoFuturoDTO(TipoEvento tipo, Integer pagina, Integer total) {
        super(null, DateUtil.getDataAtual(), tipo, null, pagina, total);
    }
    
}
