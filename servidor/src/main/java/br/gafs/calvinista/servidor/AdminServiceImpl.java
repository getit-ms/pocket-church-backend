/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.FiltroIgreja;
import br.gafs.calvinista.dto.FiltroIgrejaDTO;
import br.gafs.calvinista.entity.Acesso;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.entity.Plano;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AllowUsuario;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.AdminService;
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
@Interceptors({ServiceLoggerInterceptor.class, SecurityInterceptor.class})
public class AdminServiceImpl implements AdminService {
    
    @EJB
    private DAOService daoService;
    
    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Arquivo upload(String fileName, byte[] fileData) {
        return daoService.update(new Arquivo(null, fileName, fileData));
    }

    @Override
    public Arquivo buscaArquivo(Long arquivo) {
        return daoService.find(Arquivo.class, new RegistroIgrejaId(null, arquivo));
    }
    
    @Override
    @AllowUsuario
    public BuscaPaginadaDTO<Igreja> busca(FiltroIgrejaDTO filtro) {
        return daoService.findWith(new FiltroIgreja(filtro));
    }

    @Override
    @AllowUsuario
    public void cadastra(Igreja igreja) {
        Membro membro = igreja.getPrimeiroMembro();
        
        igreja.getFuncionalidadesAplicativo().
                addAll(igreja.getPlano().getFuncionalidadesMembro());
        
        igreja = daoService.create(igreja);
        
        membro.setIgreja(igreja);
        membro.setSenha(SenhaUtil.encryptSHA256("123456"));
        membro = daoService.create(membro);
        
        Perfil perfil = new Perfil(igreja);
        perfil.setFuncionalidades(igreja.getPlano().getFuncionalidadesAdmin());
        perfil.setNome("ADMIN");
        perfil = daoService.create(perfil);
        
        Acesso acesso = new Acesso(membro);
        acesso.setPerfis(Arrays.asList(perfil));
        acesso = daoService.create(acesso);
    }

    @Override
    @AllowUsuario
    public void atualiza(Igreja igreja) {
        daoService.update(igreja);
    }

    @Override
    @AllowUsuario
    public Igreja buscaIgreja(String chave) {
        return daoService.find(Igreja.class, chave);
    }

    @Override
    @AllowUsuario
    public void inativa(Long igreja) {
        // TODO
    }

    @Override
    @AllowUsuario
    public List<Plano> buscaTodos() {
        return daoService.findAll(Plano.class);
    }

    @Override
    @AllowUsuario
    public void cadastra(Plano plano) {
        daoService.create(plano);
    }

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
