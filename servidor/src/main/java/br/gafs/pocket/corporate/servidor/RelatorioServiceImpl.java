package br.gafs.pocket.corporate.servidor;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.dao.DAOService;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.pocket.corporate.entity.Documento;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.TipoEvento;
import br.gafs.pocket.corporate.security.AllowAdmin;
import br.gafs.pocket.corporate.security.AuditoriaInterceptor;
import br.gafs.pocket.corporate.security.SecurityInterceptor;
import br.gafs.pocket.corporate.service.AppService;
import br.gafs.pocket.corporate.service.RelatorioService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.servidor.relatorio.*;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.File;
import java.io.IOException;

/**
 * Created by mirante0 on 01/02/2017.
 */
@Stateless
@Local(RelatorioService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class, SecurityInterceptor.class})
public class RelatorioServiceImpl implements RelatorioService {

    private static final long LIMITE_CACHE = ResourceBundleUtil._default().getPropriedadesAsLong("LIMITE_TIMEOUT_CACHE_REPORT");

    @EJB
    private ProcessamentoService processamentoService;

    @EJB
    private AppService appService;

    @EJB
    private DAOService daoService;

    @Inject
    private SessaoBean sessaoBean;

    private File export(ProcessamentoRelatorioCache.Relatorio relatorio, String type) throws IOException, InterruptedException {
        File file = ProcessamentoRelatorioCache.file(relatorio, type);

        if (!file.exists() || System.currentTimeMillis() - file.lastModified() > LIMITE_CACHE){
            processamentoService.execute(new ProcessamentoRelatorioCache(relatorio, type));
        }

        return file;
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public File exportaInscritos(Long id, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioInscritos(appService.buscaEvento(id)), tipo);
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EVENTOS})
    public File exportaInscritos(TipoEvento tipo) throws IOException, InterruptedException {
        return export(new RelatorioTodosInscritos(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa()), tipo), "xls");
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_COLABORADORS)
    public File exportaContatos() throws IOException, InterruptedException {
        return export(new RelatorioContatos(daoService.find(Empresa.class, sessaoBean.getChaveEmpresa())), "xls");
    }

    @Override
    public File exportaDocumento(Long documento, String tipo) throws IOException, InterruptedException {
        Documento entidade = appService.buscaDocumento(documento);
        return export(new RelatorioDocumento(entidade), tipo);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public File exportaResultadosEnquete(Long enquete, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioResultadoEnquete(appService.buscaResultado(enquete)), tipo);
    }
}
