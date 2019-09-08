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
public class ParametrosGlobaisDTO implements DTO {
    @TipoParametro.Mapping(TipoParametro.REPOSITORY_URL)
    private String repositoryURL;

    @TipoParametro.Mapping(TipoParametro.SMTP_PORTA)
    private Integer smtpPort;

    @TipoParametro.Mapping(TipoParametro.SMTP_ENABLE_START_TLS)
    private Boolean enableStartTls;

    @TipoParametro.Mapping(TipoParametro.SMTP_AUTH)
    private Boolean auth;

    @TipoParametro.Mapping(TipoParametro.SMTP_PROPERTIES)
    private String properties;

    @TipoParametro.Mapping(TipoParametro.ADMIN_MAILS)
    private String adminMails;

    @TipoParametro.Mapping(TipoParametro.SMTP_USERNAME)
    private String username;

    @TipoParametro.Mapping(TipoParametro.SMTP_PASSWORD)
    private String password;

    @TipoParametro.Mapping(TipoParametro.SMTP_FROM_NAME)
    private String fromName;

    @TipoParametro.Mapping(TipoParametro.SMTP_FROM_EMAIL)
    private String fromEmail;

}
