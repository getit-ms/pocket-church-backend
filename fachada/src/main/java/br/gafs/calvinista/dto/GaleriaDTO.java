package br.gafs.calvinista.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Gabriel on 24/07/2018.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaleriaDTO {
    private String id;
    private String nome;
    private String descricao;

    private FotoDTO fotoPrimaria;

    private int quantidadeFotos;
}
