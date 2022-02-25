/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.util.date.DateUtil;

/**
 * @author Gabriel
 */
public class FiltroVotacaoAtivaDTO extends FiltroVotacaoDTO {

    public FiltroVotacaoAtivaDTO(Integer pagina, Integer total) {
        super(DateUtil.getDataAtual(), null, pagina, total);
    }

}
