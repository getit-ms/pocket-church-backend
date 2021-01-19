/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.dao.FiltroEmpresa;
import br.gafs.pocket.corporate.dto.FiltroEmpresaDTO;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.security.AllowAdmin;
import br.gafs.pocket.corporate.security.AllowUsuario;
import br.gafs.pocket.corporate.security.Audit;
import br.gafs.pocket.corporate.security.AuditoriaInterceptor;
import br.gafs.pocket.corporate.security.SecurityInterceptor;
import br.gafs.pocket.corporate.service.AdminService;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.util.senha.SenhaUtil;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

/**
 *
 * @author Gabriel
 */
@Stateless
@Local(AdminService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class, SecurityInterceptor.class})
public class AdminServiceImpl implements AdminService {
    
    @EJB
    private DAOService daoService;
    
    @Audit
    @Override
    @AllowAdmin(Funcionalidade.MANTER_COLABORADORES)
    public Arquivo upload(String fileName, byte[] fileData) {
        return daoService.update(new Arquivo(null, fileName, fileData));
    }

    @Override
    public Arquivo buscaArquivo(Long arquivo) {
        return daoService.find(Arquivo.class, new RegistroEmpresaId(null, arquivo));
    }
    
    @Override
    @AllowUsuario
    public BuscaPaginadaDTO<Empresa> busca(FiltroEmpresaDTO filtro) {
        return daoService.findWith(new FiltroEmpresa(filtro));
    }

    @Audit
    @Override
    @AllowUsuario
    public void cadastra(Empresa empresa) {
        Colaborador colaborador = empresa.getPrimeiroColaborador();
        
        empresa.getFuncionalidadesAplicativo().
                addAll(empresa.getPlano().getFuncionalidadesColaborador());
        
        empresa = daoService.create(empresa);
        
        colaborador.setEmpresa(empresa);
        colaborador.setSenha(SenhaUtil.encryptSHA256("123456"));
        colaborador = daoService.create(colaborador);
        
        Perfil perfil = new Perfil(empresa);
        perfil.setFuncionalidades(empresa.getPlano().getFuncionalidadesAdmin());
        perfil.setNome("ADMIN");
        perfil = daoService.create(perfil);
        
        Acesso acesso = new Acesso(colaborador);
        acesso.setPerfis(Arrays.asList(perfil));
        acesso = daoService.create(acesso);
    }

    @Audit
    @Override
    @AllowUsuario
    public void atualiza(Empresa empresa) {
        daoService.update(empresa);
    }

    @Override
    @AllowUsuario
    public Empresa buscaEmpresa(String chave) {
        return daoService.find(Empresa.class, chave);
    }

    @Audit
    @Override
    @AllowUsuario
    public void inativa(Long empresa) {
        // TODO
    }

    @Override
    @AllowUsuario
    public List<Plano> buscaTodos() {
        return daoService.findAll(Plano.class);
    }

    @Audit
    @Override
    @AllowUsuario
    public void cadastra(Plano plano) {
        daoService.create(plano);
    }

    @Audit
    @Override
    @AllowUsuario
    public void atualiza(Plano plano) {
        daoService.update(plano);
    }

    @Override
    @AllowUsuario
    public Plano buscaPlano(Long id) {
        return daoService.find(Plano.class, id);
    }

    
}
