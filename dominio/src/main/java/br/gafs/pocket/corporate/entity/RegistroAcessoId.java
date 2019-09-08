package br.gafs.pocket.corporate.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"data", "funcionalidade", "dispositivo", "empresa"})
public class RegistroAcessoId implements Serializable {
    private Date data;
    private Integer funcionalidade;
    private String dispositivo;
    private String empresa;
}
