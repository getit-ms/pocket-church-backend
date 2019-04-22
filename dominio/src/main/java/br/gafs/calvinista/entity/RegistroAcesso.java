package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.entity.domain.StatusRegistroAcesso;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RegistroAcessoId.class)
@Table(name = "tb_registro_acesso")
@EqualsAndHashCode(of = {"data", "igreja", "funcionalidade", "dispositivo"})
@NamedQueries({
        @NamedQuery(name = "RegistroAcesso.removeAntigas", query = "delete from RegistroAcesso e where e.data < :limite")
})
public class RegistroAcesso implements IEntity {
    @Id
    @Column(name = "data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Id
    @Column(name = "funcionalidade")
    private Integer funcionalidade;

    @Id
    @Column(name = "dispositivo")
    private String dispositivo;

    @Id
    @Column(name = "chave_igreja")
    private String igreja;

    @Column(name = "status")
    private StatusRegistroAcesso status;

    @Override
    public RegistroAcessoId getId() {
        return new RegistroAcessoId(data, funcionalidade, dispositivo, igreja);
    }
}
