package br.gafs.calvinista.dto;

import br.gafs.calvinista.entity.domain.Funcionalidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class MenuDTO implements Comparable<MenuDTO> {
    private String nome;
    private String icone;
    @JsonIgnore
    private Integer ordem;
    private String link;
    private Funcionalidade funcionalidade;
    private List<MenuDTO> submenus = new ArrayList<>();

    @Override
    public int compareTo(MenuDTO o) {
        return ordem.compareTo(o.ordem);
    }
}
