package br.gafs.calvinista.servidor;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AuditoriaInterceptor;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.RelatorioService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.servidor.relatorio.RelatorioEstudo;
import br.gafs.calvinista.servidor.relatorio.RelatorioHino;
import br.gafs.calvinista.servidor.relatorio.RelatorioInscritos;
import br.gafs.dao.DAOService;
import br.gafs.logger.ServiceLoggerInterceptor;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by mirante0 on 01/02/2017.
 */
@Stateless
@Local(RelatorioService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class, SecurityInterceptor.class})
public class RelatorioServiceImpl implements RelatorioService {

    @EJB
    private ProcessamentoService processamentoService;

    @EJB
    private AppService appService;

    @EJB
    private DAOService daoService;

    @Inject
    private SessaoBean sessaoBean;

    private File export(ProcessamentoRelatorioCache.Relatorio relatorio, String type, Date timeout) throws IOException, InterruptedException {
        File file = ProcessamentoRelatorioCache.file(relatorio, type);

        if (!file.exists() || file.lastModified() < timeout.getTime()){
            processamentoService.execute(new ProcessamentoRelatorioCache(relatorio, type));
        }

        return file;
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EBD, Funcionalidade.MANTER_EVENTOS})
    public File exportaInscritos(Long id, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioInscritos(appService.buscaEvento(id), sessaoBean.getIdMembro()), tipo, new Date());
    }

    @Override
    public File exportaHino(Long hino, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioHino(
                daoService.find(Igreja.class, sessaoBean.getChaveIgreja()),
                appService.buscaHino(hino)), tipo, new Date());
    }



    @Override
    public File exportaEstudo(Long estudo, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioEstudo(
                appService.buscaEstudo(estudo)), tipo, new Date());
    }
}
