/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.RegistroEmpresaId;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.security.*;
import br.gafs.pocket.corporate.service.ArquivoService;
import br.gafs.dao.DAOService;
import br.gafs.logger.ServiceLoggerInterceptor;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

/**
 *
 * @author Gabriel
 */
@Stateless
@Local(ArquivoService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class, SecurityInterceptor.class})
public class ArquivoServiceImpl implements ArquivoService {

    @EJB
    private DAOService daoService;
    
    @Inject
    private SessaoBean sessaoBean;
    
    @Override
    public Arquivo buscaArquivo(Long arquivo) {
        return daoService.find(Arquivo.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), arquivo));
    }

    @Override
    @AllowColaborador
    @AllowAdmin({
            Funcionalidade.MANTER_COLABORADORES,
            Funcionalidade.MANTER_BOLETINS,
            Funcionalidade.MANTER_PUBLICACOES,
            Funcionalidade.MANTER_DOCUMENTOS,
            Funcionalidade.MANTER_DADOS_INSTITUCIONAIS,
            Funcionalidade.MANTER_EVENTOS,
            Funcionalidade.MANTER_NOTICIAS,
            Funcionalidade.MANTER_CLASSIFICADOS
    })
    public Arquivo upload(String fileName, byte[] fileData) {
        return cadastra(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()), fileName, fileData);
    }
    
    @Override
    public Arquivo cadastra(Empresa empresa, String fileName, byte[] fileData) {
        return daoService.update(new Arquivo(empresa, fileName, fileData));
    }

    @Schedule(hour = "0", minute = "0", second = "0", persistent = false)
    public void removeArquivosEmDesuso() {
        List<Arquivo> arquivos = daoService.findWith(QueryAdmin.ARQUIVOS_VENCIDOS.create());
        for (Arquivo arquivo : arquivos) {
            daoService.delete(Arquivo.class, new RegistroEmpresaId(arquivo.getEmpresa().getChave(), arquivo.getId()));
        }
    }

    @Override
    public void registraDesuso(Long idArquivo) {
        registraDesuso(sessaoBean.getChaveEmpresa(), idArquivo);
    }

    @Override
    public void registraDesuso(String empresa, Long idArquivo) {
        daoService.execute(QueryAdmin.REGISTRA_DESUSO_ARQUIVO.create(idArquivo, sessaoBean.getChaveEmpresa()));
    }

    @Override
    public void registraUso(Long idArquivo) {
        registraUso(sessaoBean.getChaveEmpresa(), idArquivo);
    }

    @Override
    public void registraUso(String empresa, Long idArquivo) {
        daoService.execute(QueryAdmin.REGISTRA_USO_ARQUIVO.create(idArquivo, sessaoBean.getChaveEmpresa()));
    }
    
    
}
