package br.gafs.calvinista.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "chaveIgreja"})
public class VideoId implements Serializable {
    private String id;
    private String chaveIgreja;
}
