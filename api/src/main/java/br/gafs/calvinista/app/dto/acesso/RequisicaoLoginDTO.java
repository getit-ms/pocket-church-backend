/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.dto.acesso;

import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.dto.DTO;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class RequisicaoLoginDTO implements DTO {
    private String username;
    private String password;
    private TipoDispositivo tipoDispositivo;
    private String version;
}
