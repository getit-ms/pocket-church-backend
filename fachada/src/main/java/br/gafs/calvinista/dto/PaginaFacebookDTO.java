package br.gafs.calvinista.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Gabriel on 16/11/2018.
 */
@Getter
@AllArgsConstructor
public class PaginaFacebookDTO {
    private String id;

    @JsonProperty("nome")
    private String name;

    @JsonIgnore
    private String access_token;
}
