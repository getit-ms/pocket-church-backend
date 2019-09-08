package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.Arquivo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Gabriel on 05/10/2018.
 */
@Getter
@AllArgsConstructor
public class ResumoEmpresaDTO {
    private String chave;
    private String nome;
    private String nomeAplicativo;
    private Arquivo logoPequena;
    private String cidade;
    private String estado;

    public ResumoEmpresaDTO(String chave, String nome, String nomeAplicativo, Arquivo logoPequena) {
        this(chave, nome, nomeAplicativo, logoPequena, null, null);
    }
}
