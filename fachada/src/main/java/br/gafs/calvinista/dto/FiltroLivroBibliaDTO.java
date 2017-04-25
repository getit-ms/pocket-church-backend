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
public class FiltroLivroBibliaDTO implements DTO {
    private Date ultimaAtualizacao;
    private Integer pagina;
    private Integer total;
    
    public void setUltimaAtualizacao(Date ultimaAtualizacao){
        if (ultimaAtualizacao != null &&
                ultimaAtualizacao.getTime() > System.currentTimeMillis()){
            this.ultimaAtualizacao = new Date();
        }else{
            this.ultimaAtualizacao = ultimaAtualizacao;
        }
    }
    
}
