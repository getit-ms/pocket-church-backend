package br.gafs.pocket.corporate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

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
    private Date dataAtualizacao;

    private FotoDTO fotoPrimaria;

    private int quantidadeFotos;
}
