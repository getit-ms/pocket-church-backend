package br.gafs.calvinista.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValorInscricaoEventoId implements Serializable {
    private String nome;
    private InscricaoEventoId inscricao;
}
