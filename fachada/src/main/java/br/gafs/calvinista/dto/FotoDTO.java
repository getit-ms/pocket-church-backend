package br.gafs.calvinista.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Gabriel on 24/07/2018.
 */
@Getter
@Builder
public class FotoDTO {
    private String id;
    private String server;
    private String farm;
    private String secret;
    private String titulo;
}
