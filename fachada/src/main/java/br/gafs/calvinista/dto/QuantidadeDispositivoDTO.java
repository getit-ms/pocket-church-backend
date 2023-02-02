package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.TipoDispositivo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuantidadeDispositivoDTO {
    private TipoDispositivo tipo;
    private Long quantidadeDispositivos;
    private Long quantidadeLogados;
}
