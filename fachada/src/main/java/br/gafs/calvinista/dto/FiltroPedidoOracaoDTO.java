/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.StatusPedidoOracao;
import br.gafs.dto.DTO;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPedidoOracaoDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    private List<StatusPedidoOracao> status = Arrays.asList(StatusPedidoOracao.PENDENTE);
    private Integer pagina = 1;
    private Integer total = 10;
}
