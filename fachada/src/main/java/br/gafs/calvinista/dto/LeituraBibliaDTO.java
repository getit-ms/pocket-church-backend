/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.DiaLeituraBiblica;
import br.gafs.calvinista.entity.MarcacaoLeituraBiblica;
import br.gafs.dto.DTO;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
public class LeituraBibliaDTO implements DTO {
    private DiaLeituraBiblica dia;
    private Date ultimaAlteracao;
    private Long plano;
    private boolean lido;
    
    public LeituraBibliaDTO(DiaLeituraBiblica dia){
        this.dia = dia;
        this.plano = dia.getPlano().getId();
        this.ultimaAlteracao = dia.getPlano().getUltimaAlteracao();
    }
    
    public LeituraBibliaDTO(DiaLeituraBiblica dia, boolean lido){
        this(dia);
        this.lido = lido;
        this.ultimaAlteracao = new Date();
    }
    
    public void setLido(MarcacaoLeituraBiblica marcacao){
        if (marcacao != null){
            this.lido = true;
            
            if (this.ultimaAlteracao.before(marcacao.getData())){
                this.ultimaAlteracao = marcacao.getData();
            }
        }else{
            this.lido = false;
        }
    }
}
