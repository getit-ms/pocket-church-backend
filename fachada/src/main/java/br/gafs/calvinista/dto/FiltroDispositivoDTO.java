/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.HorasEnvioVersiculo;
import br.gafs.dto.DTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
@RequiredArgsConstructor
public class FiltroDispositivoDTO implements DTO {
    private List<Long> ministerios = new ArrayList<Long>();
    private Integer pagina = 1;
    private HorasEnvioVersiculo hora;
    private Long membro;
    private final Igreja igreja;

    public FiltroDispositivoDTO(Igreja igreja, Long membro) {
        this(igreja);
        this.membro = membro;
    }
    
    public FiltroDispositivoDTO(Igreja igreja, HorasEnvioVersiculo hora) {
        this(igreja);
        this.hora = hora;
    }
    
    public void proxima(){
        pagina++;
    }
}
