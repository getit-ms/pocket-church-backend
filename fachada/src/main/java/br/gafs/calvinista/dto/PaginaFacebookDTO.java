package br.gafs.calvinista.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.restfb.Facebook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Gabriel on 16/11/2018.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaginaFacebookDTO {
    @Facebook
    private String id;

    @Facebook("name")
    private String nome;

    @JsonIgnore
    @Facebook("access_token")
    private String acessToken;

    @JsonIgnore
    @Facebook("category")
    private String categoria;
}
