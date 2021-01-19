package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuantidadeDispositivoDTO {
    private TipoDispositivo tipo;
    private Long quantidadeDispositivos;
    private Long quantidadeLogados;
}
