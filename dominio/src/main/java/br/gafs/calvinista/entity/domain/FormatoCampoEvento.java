package br.gafs.calvinista.entity.domain;

public enum FormatoCampoEvento {
    NENHUM,

    // texto
    CEP,
    CPF_CNPJ,
    TELEFONE,

    // numérico
    NUMERO_INTEIRO,
    NUMERO_REAL,
    MONETARIO,

    // anexo
    IMAGEM,
}
