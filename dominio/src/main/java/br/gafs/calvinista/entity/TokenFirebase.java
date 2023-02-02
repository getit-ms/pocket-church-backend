package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tb_token_firebase")
public class TokenFirebase implements IEntity {
    @Id
    @Column(name = "id_token_firebase")
    @SequenceGenerator(name = "seq_token_firebase", sequenceName = "seq_token_firebase")
    @GeneratedValue(generator = "seq_token_firebase", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "versao")
    private String versao;

    @Column(name = "token")
    private String token;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;
}
