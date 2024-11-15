/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.VersiculoDiario;
import br.gafs.dto.DTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@Data
public class StatusAdminDTO implements DTO{
    private VersiculoDiario versiculoDiario;
    private List<NotificacaoDTO> notificacoes = new ArrayList<NotificacaoDTO>();
    
    public NotificacaoDTO addNotificacao(String mensagem){
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setMensagem(mensagem);
        notificacoes.add(dto);
        return dto;
    }
    
    public NotificacaoDTO addNotificacao(String mensagem, Integer count, Map<String, Object> args, String path){
        NotificacaoDTO dto = addNotificacao(mensagem);
        dto.setCount(count);
        dto.setPath(path);
        dto.getArgs().putAll(args);
        return dto;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class NotificacaoDTO implements DTO {
        private String mensagem;
        private Integer count = 1;
        private Map<String, Object> args = new HashMap<String, Object>();
        private String path;
    }
}
