package br.gafs.calvinista.servidor;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.entity.Hino;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AuditoriaInterceptor;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.RelatorioService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.servidor.relatorio.*;
import br.gafs.dao.DAOService;
import br.gafs.logger.ServiceLoggerInterceptor;

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
    @AllowAdmin({Funcionalidade.MANTER_EBD, Funcionalidade.MANTER_EVENTOS})
    public File exportaInscritos(Long id, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioInscritos(appService.buscaEvento(id)), tipo);
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EBD, Funcionalidade.MANTER_EVENTOS})
    public File exportaInscritos(TipoEvento tipo) throws IOException, InterruptedException {
        return export(new RelatorioTodosInscritos(daoService.find(Igreja.class, sessaoBean.getChaveIgreja()), tipo), "xls");
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_MEMBROS)
    public File exportaContatos() throws IOException, InterruptedException {
        return export(new RelatorioContatos(daoService.find(Igreja.class, sessaoBean.getChaveIgreja())), "xls");
    }

    @Override
    public File exportaHino(Long hino, String tipo) throws IOException, InterruptedException {
        Hino entidade = appService.buscaHino(hino);
        return export(new RelatorioHino(
                daoService.find(Igreja.class, sessaoBean.getChaveIgreja()),
                entidade), tipo);
    }

    @Override
    public File exportaEstudo(Long estudo, String tipo) throws IOException, InterruptedException {
        Estudo entidade = appService.buscaEstudo(estudo);
        return export(new RelatorioEstudo(entidade), tipo);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public File exportaResultadosVotacao(Long votacao, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioResultadoVotacao(appService.buscaResultado(votacao)), tipo);
    }
}
