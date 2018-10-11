package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Arquivo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Gabriel on 05/10/2018.
 */
@Getter
@AllArgsConstructor
public class ResumoIgrejaDTO {
    private String chave;
    private String nome;
    private String nomeAplicativo;
    private Arquivo logoPequena;
    private String cidade;
    private String estado;
}
