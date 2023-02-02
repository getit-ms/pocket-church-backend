package br.gafs.calvinista.entity.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoItemEvento {
    BOLETIM,
    ESTUDO,
    AUDIO,
    NOTICIA,
    EVENTO_CALENDARIO,
    EVENTO_INSCRICAO,
    VIDEO,
    FOTOS,
    EBD,
    CULTO,
    PUBLICACAO;

    public static TipoItemEvento[] TIPOS_PUBLICOS = {
            BOLETIM,
            ESTUDO,
            AUDIO,
            NOTICIA,
            EVENTO_CALENDARIO,
            VIDEO,
            CULTO,
    };
}
