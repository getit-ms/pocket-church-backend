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
@EqualsAndHashCode(of = {"empresa", "data", "funcionalidade"})
public class EstatisticaAcessoId implements Serializable {
    private String empresa;
    private Date data;
    private Integer funcionalidade;
}
