package br.gafs.pocket.corporate.dto;

import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.pocket.corporate.entity.Audio;
import br.gafs.pocket.corporate.entity.CategoriaAudio;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Gabriel on 07/08/2018.
 */
@Getter
@Setter
public class BuscaPaginadaAudioDTO extends BuscaPaginadaDTO<Audio> {
    private CategoriaAudio categoria;

    public BuscaPaginadaAudioDTO(List<Audio> resultados, long totalResultados, int pagina, int registroPorPagina) {
        super(resultados, totalResultados, pagina, registroPorPagina);
    }
}
