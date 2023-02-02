package br.gafs.calvinista.servidor.batch.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Gabriel on 23/11/2018.
 */
@Getter
@RequiredArgsConstructor
public class LoginDTO {
    private final String uuid;
    private final String tokenAcesso;
    private final String versao;
}
