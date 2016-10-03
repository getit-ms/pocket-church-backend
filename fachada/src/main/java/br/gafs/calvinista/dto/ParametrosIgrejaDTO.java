/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.entity.domain.TipoParametro.Mapping;
import br.gafs.dto.DTO;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ParametrosIgrejaDTO implements DTO {
    @Mapping(TipoParametro.PUSH_ANDROID_KEY)
    private String pushAndroidKey;
    @Mapping(TipoParametro.PUSH_IOS_CERTIFICADO)
    private byte[] pushIosCertificado;
    @Mapping(TipoParametro.PUSH_IOS_PASS)
    private String pushIosPass;
}
