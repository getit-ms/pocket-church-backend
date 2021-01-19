package br.gafs.pocket.corporate.dto;

import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
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
    @Setter
    private long notificacoes;
    private Funcionalidade funcionalidade;
    private List<MenuDTO> submenus = new ArrayList<>();

    public void add(MenuDTO submenu) {
        submenus.add(submenu);
        Collections.sort(submenus);
        this.notificacoes += submenu.getNotificacoes();
    }

    @Override
    public int compareTo(MenuDTO o) {
        return ordem.compareTo(o.ordem);
    }
}
