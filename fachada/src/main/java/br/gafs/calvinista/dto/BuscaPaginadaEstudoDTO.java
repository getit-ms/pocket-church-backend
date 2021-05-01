package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.CategoriaEstudo;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.dao.BuscaPaginadaDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Gabriel on 19/02/2018.
 */
@Getter
public class BuscaPaginadaEstudoDTO extends BuscaPaginadaDTO<Estudo> {
    @Setter
    private CategoriaEstudo categoria;

    public BuscaPaginadaEstudoDTO(List<Estudo> resultados, long totalResultados, int pagina, int registroPorPagina) {
        super(resultados, totalResultados, pagina, registroPorPagina);
    }
}
