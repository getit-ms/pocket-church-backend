/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoBoletim;
import br.gafs.util.date.DateUtil;

/**
 *
 * @author Gabriel
 */
public class FiltroBoletimPublicadoDTO extends FiltroBoletimDTO {

    public FiltroBoletimPublicadoDTO(String filtro, TipoBoletim tipo, Integer pagina, Integer total) {
        super(filtro, null, DateUtil.getDataAtual(), tipo, pagina, total);
    }
    
}
