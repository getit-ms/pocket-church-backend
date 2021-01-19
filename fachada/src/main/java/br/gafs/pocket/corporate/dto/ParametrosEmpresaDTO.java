/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.dto.DTO;
import lombok.Data;

/**
 *
 * @author Gabriel
 */
@Data
public class ParametrosEmpresaDTO implements DTO {
    @TipoParametro.Mapping(TipoParametro.PUSH_ANDROID_KEY)
    private String pushAndroidKey;
    @TipoParametro.Mapping(TipoParametro.PUSH_IOS_CERTIFICADO)
    private byte[] pushIosCertificado;
    @TipoParametro.Mapping(TipoParametro.PUSH_IOS_PASS)
    private String pushIosPass;
}
