package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.StatusDiaDevocionario;
import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class FiltroDevocionarioDTO implements DTO {
    private Date dataInicio;
    private Date dataTermino;
    private StatusDiaDevocionario status;
    private Integer pagina = 1;
    private Integer total = 10;
}
