package br.gafs.calvinista.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idMembro", "idTermoAceite", "chaveIgreja"})
public class AceiteTermoMembroId implements Serializable {
    private Long idMembro;
    private Long idTermoAceite;
    private String chaveIgreja;
}
