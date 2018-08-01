/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.entity;

import br.gafs.bean.IEntity;
import br.gafs.pocket.corporate.entity.domain.StatusColaborador;
import br.gafs.pocket.corporate.view.View;
import br.gafs.pocket.corporate.view.View.Detalhado;
import br.gafs.pocket.corporate.view.View.Resumido;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.date.DateUtil;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Gabriel
 */
@Data
@Entity
@NoArgsConstructor
@ToString(of = "id")
@Table(name = "tb_colaborador")
@IdClass(RegistroEmpresaId.class)
@EqualsAndHashCode(of = {"id", "empresa"})
@NamedQueries({
    @NamedQuery(name = "Colaborador.autentica", query = "select m from Colaborador m where upper(m.email) = upper(:email) and m.empresa.chave = :empresa and upper(m.senha) = upper(:senha) and m.status in :status"),
    @NamedQuery(name = "Colaborador.findGerente", query = "select m from Colaborador m where m.gerente = true and m.empresa.chave = :chaveEmpresa and m.status in :status and m.id not in (select ca.gerente.id from CalendarioAtendimento ca where ca.empresa.chave = :chaveEmpresa and ca.status = :statusCalendario) order by m.nome"),
    @NamedQuery(name = "Colaborador.findByEmailEmpresa", query = "select m from Colaborador m where upper(m.email) = upper(:email) and m.empresa.chave = :empresa and m.status in :status"),
    @NamedQuery(name = "Colaborador.findFuncionalidadesAcessoApp", query = "select f from Colaborador m inner join m.empresa i inner join i.funcionalidadesAplicativo f where m.id = :colaborador and i.chave = :empresa group by f"),
    @NamedQuery(name = "Colaborador.findFuncionalidadesAcessoAdmin", query = "select f from Colaborador m inner join m.empresa i inner join i.plano p inner join p.funcionalidades f inner join m.acesso a inner join a.perfis pr inner join pr.funcionalidades fu where fu = f and m.id = :colaborador and i.chave = :empresa group by f")
})
public class Colaborador implements IEntity {
    @Id
    @JsonView(Resumido.class)
    @Column(name = "id_colaborador")
    @SequenceGenerator(sequenceName = "seq_colaborador", name = "seq_colaborador")
    @GeneratedValue(generator = "seq_colaborador", strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @NotEmpty
    @Length(max = 150)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
    
    @NotEmpty
    @JsonIgnore
    @Length(max = 250)
    @Column(name = "senha", length = 250, nullable = false)
    private String senha = "undefined";
    
    @Email
    @NotEmpty
    @Length(max = 150)
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "email", length = 150, nullable = false)
    private String email;
    
    @JsonView(Detalhado.class)
    @Setter(AccessLevel.NONE)
    @Column(name = "deve_alterar_senha", length = 150, nullable = false)
    private boolean deveAlterarSenha;
    
    @JsonView(Detalhado.class)
    @Temporal(TemporalType.DATE)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "data_nascimento")
    private Date dataNascimento;
    
    @ElementCollection
    @JsonView({Detalhado.class,Aniversariante.class})
    @Column(name = "telefone")
    @View.MergeViews(View.Edicao.class)
    @CollectionTable(name = "rl_telefone_colaborador",
            joinColumns = {
                @JoinColumn(name = "id_colaborador", referencedColumnName = "id_colaborador"),
                @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa")
            })
    private List<String> telefones = new ArrayList<String>();
    
    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private StatusColaborador status = StatusColaborador.CONTATO;
    
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "gerente", nullable = false)
    private boolean gerente;
    
    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "visitante", nullable = false)
    private boolean visitante;
    
    @JsonView(Detalhado.class)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_endereco", nullable = false)
    private Endereco endereco = new Endereco();
    
    @JsonView(Detalhado.class)
    @Column(name = "data_exclusao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;
    
    @Id
    @JsonIgnore
    @Column(name = "chave_empresa", insertable = false, updatable = false)
    private String chaveEmpresa;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chave_empresa", nullable = false)
    private Empresa empresa;
    
    @JsonIgnore
    @OneToOne(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private Acesso acesso;

    @JsonView(Detalhado.class)
    @View.MergeViews(View.Edicao.class)
    @Column(name = "dados_disponiveis", nullable = false)
    private boolean dadosDisponiveis = true;

    @Getter(onMethod = @_({
        @JsonProperty,
        @JsonView(Detalhado.class)
    }))
    @Column(name = "deseja_disponibilizar_dados", nullable = false)
    private boolean desejaDisponibilizarDados = true;

    @Setter
    @OneToOne
    @JsonView(Resumido.class)
    @View.MergeViews(View.Edicao.class)
    @JoinColumns({
            @JoinColumn(name = "id_foto", referencedColumnName = "id_arquivo"),
            @JoinColumn(name = "chave_empresa", referencedColumnName = "chave_empresa", insertable = false, updatable = false)
    })
    private Arquivo foto;

    @Transient
    @View.MergeViews(View.AlterarSenha.class)
    private String novaSenha;
    
    @Transient
    @View.MergeViews(View.AlterarSenha.class)
    private String confirmacaoSenha;
    
    public Colaborador(Empresa empresa) {
        this.empresa = empresa;
    }
    
    @JsonIgnore
    public void setDesejaDisponibilizarDados(boolean deseja){
        if (deseja != isDadosDisponiveis()){
            this.desejaDisponibilizarDados = this.dadosDisponiveis = deseja;
        }
    }
    
    public void setDadosDisponiveis(boolean dadosDisponiveis){
        if (this.dadosDisponiveis != dadosDisponiveis){
            if (dadosDisponiveis && !desejaDisponibilizarDados){
                throw new ServiceException("mensagens.MSG-044");
            }
        
            this.dadosDisponiveis = dadosDisponiveis;
        }
    }
    
    public boolean isDadosDisponiveis(){
        return isDesejaDisponibilizarDados() && dadosDisponiveis;
    }
    
    @JsonIgnore
    public boolean isSenhaUndefined(){
        return "undefined".equals(senha);
    }
    
    public boolean isTemAcesso(){
        return acesso != null;
    }
    
    public void addTelefone(String telefone){
        telefones.add(telefone);
    }
    
    public void removeTelefone(String telefone){
        telefones.remove(telefone);
    }
    
    public boolean isColaborador(){
        return StatusColaborador.COLABORADOR.equals(status);
    }
    
    public boolean isAdmin(){
        return isColaborador() && acesso != null;
    }
    
    public void exclui(){
        status = StatusColaborador.EXCLUIDO;
        dataExclusao = new Date();
    }
    
    public boolean isContato(){
        return StatusColaborador.CONTATO.equals(status);
    }
    
    public void contato(){
        if (isColaborador()){
            status = StatusColaborador.CONTATO;
            acesso = null;
        }
    }
    
    public void retiraAdmin(){
        acesso = null;
    }
    
    public void alteraSenha(){
        if (!StringUtil.isEmpty(novaSenha) &&
                novaSenha.equals(confirmacaoSenha)){
            senha = SenhaUtil.encryptSHA256(novaSenha);
            deveAlterarSenha = false;
        }else{
            throw new ServiceException("mensagens.MSG-030");
        }
    }
    
    public boolean colaborador(){
        if (!isColaborador()){
            visitante = false;
            status = StatusColaborador.COLABORADOR;
            
            if ("undefined".equals(senha)){
                senha = SenhaUtil.geraSenha(8);
                deveAlterarSenha = true;
                return true;
            }
        }
        return false;
    }

    @JsonView(Aniversariante.class)
    public Integer getDiaAniversario() {
        if (getDataNascimento() != null) {
            return Integer.parseInt(DateUtil.formataData(getDataNascimento(), "MMdd"));
        }

        return null;
    }

    public interface Aniversariante extends Resumido {}
}
