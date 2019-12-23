package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.StatusDiaDevocionario;

import java.util.Date;

public class FiltroDevocionarioPublicadoDTO extends FiltroDevocionarioDTO {
    public FiltroDevocionarioPublicadoDTO(Date dataInicio, Date dataTermino, Integer pagina, Integer total) {
        super(dataInicio, dataTermino, StatusDiaDevocionario.PUBLICADO, pagina, total);
    }
}
