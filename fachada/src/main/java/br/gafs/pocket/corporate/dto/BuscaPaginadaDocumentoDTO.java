package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.CategoriaDocumento;
import br.gafs.pocket.corporate.entity.Documento;
import br.gafs.dao.BuscaPaginadaDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Gabriel on 19/02/2018.
 */
@Getter
public class BuscaPaginadaDocumentoDTO extends BuscaPaginadaDTO<Documento> {
    @Setter
    private CategoriaDocumento categoria;

    public BuscaPaginadaDocumentoDTO(List<Documento> resultados, long totalResultados, int pagina, int registroPorPagina) {
        super(resultados, totalResultados, pagina, registroPorPagina);
    }
}
