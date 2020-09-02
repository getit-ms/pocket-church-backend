package br.gafs.calvinista.entity;

import br.gafs.calvinista.entity.domain.TipoItemTimeline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "chaveIgreja", "tipo"})
public class ItemTimelineId implements Serializable {
    private Long id;
    private String chaveIgreja;
    private TipoItemTimeline tipo;
}
