/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity;

import br.gafs.bean.IEntity;
import br.gafs.calvinista.view.View.Detalhado;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.file.Attachment;
import br.gafs.file.EntityFileManager;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Gabriel
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "tb_arquivo")
@ToString(of = {"id", "igreja"})
@IdClass(RegistroIgrejaId.class)
@EqualsAndHashCode(of = {"id", "igreja"})
@EntityListeners(EntityFileManager.class)
@NamedQueries({
    @NamedQuery(name = "Arquivo.findVencidos", query = "select a from Arquivo a where a.timeout <= CURRENT_DATE"),
    @NamedQuery(name = "Arquivo.registraDesuso", query = "update Arquivo a set a.timeout = :timeout where a.id = :arquivo and a.igreja.chave = :igreja"),
    @NamedQuery(name = "Arquivo.registraUso", query = "update Arquivo a set a.timeout = null where a.id = :arquivo and a.igreja.chave = :igreja"),
})
public class Arquivo implements IEntity, Comparable<Arquivo> {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_arquivo")
    @SequenceGenerator(name = "seq_arquivo", sequenceName = "seq_arquivo")
    @GeneratedValue(generator = "seq_arquivo", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @JsonView(Detalhado.class)
    @Column(name = "nome", length = 150, nullable = false, updatable = false)
    private String nome;
    
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timeout", updatable = false)
    private Date timeout = new Date(System.currentTimeMillis() + DateUtil.MILESIMOS_POR_DIA);
    
    @JsonIgnore
    @Attachment(load = false, root = "/calvin/files")
    @Column(name = "dados", updatable = false, insertable = false)
    private byte[] dados;
    
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_igreja")
    private Igreja igreja;

    public Arquivo(Igreja igreja, String nome, byte[] dados) {
        this.igreja = igreja;
        this.nome = format(nome);
        this.dados = dados;
    }
    
    public void used(){
        timeout = null;
    }
    
    private static String format(String nome){
        if (nome.length() > 150){
            String extension = nome.substring(nome.lastIndexOf("."));
            nome = nome.substring(0, 150 - extension.length()) + extension;
        }
        return nome;
    }

    @JsonIgnore
    public boolean isUsed() {
        return timeout == null;
    }
    
    public void clearDados(){
        this.dados = null;
    }

    @Override
    public int compareTo(Arquivo o) {
        return getNome().compareTo(o.getNome());
    }
}
