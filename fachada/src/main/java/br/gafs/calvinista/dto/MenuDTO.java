package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.Funcionalidade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gabriel on 23/02/2018.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {
    private String nome;
    private String icone;
    private String link;
    private Funcionalidade funcionalidade;
    private List<MenuDTO> submenus = new ArrayList<>();
}
