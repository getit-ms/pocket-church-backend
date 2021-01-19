/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoBoletimInformativo;
import br.gafs.util.date.DateUtil;

/**
 *
 * @author Gabriel
 */
public class FiltroBoletimPublicadoDTO extends FiltroBoletimDTO {

    public FiltroBoletimPublicadoDTO(String filtro, TipoBoletimInformativo tipo, Integer pagina, Integer total) {
        super(filtro, null, DateUtil.getDataAtual(), tipo, pagina, total);
    }
    
}
