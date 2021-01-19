/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.file.Attachment;
import br.gafs.file.EntityFileManager;
import br.gafs.pocket.corporate.view.View;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Date;
import javax.persistence.*;

import lombok.*;

/**
 *
 * @author Gabriel
 */
@Entity
@Getter
@Cacheable
@NoArgsConstructor
@Table(name = "tb_arquivo")
@ToString(of = {"id", "empresa"})
@IdClass(RegistroEmpresaId.class)
@EqualsAndHashCode(of = {"id", "empresa"})
@EntityListeners(EntityFileManager.class)
@NamedQueries({
    @NamedQuery(name = "Arquivo.findVencidos", query = "select a from Arquivo a where a.id not in (select aa.id from Arquivo aa where aa.id = a.id and aa.timeout is null) and a.timeout <= CURRENT_DATE"),
    @NamedQuery(name = "Arquivo.registraDesuso", query = "update Arquivo a set a.timeout = :timeout where a.id = :arquivo and a.empresa.chave = :empresa"),
    @NamedQuery(name = "Arquivo.registraUso", query = "update Arquivo a set a.timeout = null where a.id = :arquivo and a.empresa.chave = :empresa"),
})
public class Arquivo implements IEntity, Comparable<Arquivo> {
    @Id
    @Setter
    @JsonView(View.Resumido.class)
    @Column(name = "id_arquivo")
    @SequenceGenerator(name = "seq_arquivo", sequenceName = "seq_arquivo")
    @GeneratedValue(generator = "seq_arquivo", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @JsonView(View.Detalhado.class)
    @Column(name = "nome", length = 150, nullable = false, updatable = false)
    private String nome;
    
    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timeout", updatable = false)
    private Date timeout = new Date(System.currentTimeMillis() + DateUtil.MILESIMOS_POR_DIA);
    
    @Transient
    @JsonIgnore
    @Attachment(load = false, root = "/pcorporate/files")
    private byte[] dados;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa")
    private Empresa empresa;

    public Arquivo(Empresa empresa, String nome, byte[] dados) {
        this.empresa = empresa;
        this.nome = format(nome);
        this.dados = dados;
    }
    
    public String getFilename(){
        return StringUtil.formataValor(nome, true, false).replace(" ", "_");
    }
    
    public void used(){
        timeout = null;
    }
    
    private static String format(String nome){
        nome = nome.replaceAll(" +", "_");

        if (nome.length() > 150){
            String extension = nome.substring(nome.lastIndexOf("."));
            nome = nome.substring(0, 150 - extension.length()) + extension;
        }

        return StringUtil.formataValor(nome, false, false);
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
