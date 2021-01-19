/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.geracao;

import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.dto.DTO;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class RequisicaoExecucao implements DTO {
    private Integer prioridade;
    private String chave;
    private List<TipoDispositivo> dispositivos;
    private String profile;
    private String mvnParams;
}
