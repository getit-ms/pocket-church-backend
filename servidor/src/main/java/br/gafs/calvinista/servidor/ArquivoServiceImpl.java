/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.ArquivoService;
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
@Interceptors({ServiceLoggerInterceptor.class, SecurityInterceptor.class})
public class ArquivoServiceImpl implements ArquivoService {

    @EJB
    private DAOService daoService;
    
    @Inject
    private SessaoBean sessaoBean;
    
    @Override
    public Arquivo buscaArquivo(Long arquivo) {
        return daoService.find(Arquivo.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), arquivo));
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public Arquivo upload(String fileName, byte[] fileData) {
        return daoService.update(new Arquivo(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()), fileName, fileData));
    }

    @Schedule(hour = "0", minute = "0", second = "0")
    public void removeArquivosEmDesuso() {
        List<Arquivo> arquivos = daoService.findWith(QueryAdmin.ARQUIVOS_VENCIDOS.create());
        for (Arquivo arquivo : arquivos) {
            daoService.delete(Arquivo.class, new RegistroIgrejaId(arquivo.getIgreja().getChave(), arquivo.getId()));
        }
    }

    @Override
    public void registraDesuso(Long idArquivo) {
        daoService.execute(QueryAdmin.REGISTRA_DESUSO_ARQUIVO.create(idArquivo, sessaoBean.getChaveIgreja()));
    }

    @Override
    public void registraUso(Long idArquivo) {
        daoService.execute(QueryAdmin.REGISTRA_USO_ARQUIVO.create(idArquivo, sessaoBean.getChaveIgreja()));
    }
    
    
}
