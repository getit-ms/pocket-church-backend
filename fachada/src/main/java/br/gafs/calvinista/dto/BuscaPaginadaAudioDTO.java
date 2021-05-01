package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Audio;
import br.gafs.calvinista.entity.CategoriaAudio;
import br.gafs.dao.BuscaPaginadaDTO;
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
