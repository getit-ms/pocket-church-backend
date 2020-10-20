package br.gafs.pocket.corporate.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "chaveEmpresa"})
public class GaleriaFotosId implements Serializable {
    private String id;
    private String chaveEmpresa;
}
