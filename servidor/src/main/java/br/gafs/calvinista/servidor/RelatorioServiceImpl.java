package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.entity.Hino;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Template;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.security.AllowAdmin;
import br.gafs.calvinista.security.AuditoriaInterceptor;
import br.gafs.calvinista.security.SecurityInterceptor;
import br.gafs.calvinista.service.AppService;
import br.gafs.calvinista.service.RelatorioService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.servidor.relatorio.*;
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

    @EJB
    private ProcessamentoService processamentoService;

    @EJB
    private AppService appService;

    @EJB
    private CustomDAOService daoService;

    @Inject
    private SessaoBean sessaoBean;

    private File export(ProcessamentoRelatorioCache.Relatorio relatorio, String type) throws IOException, InterruptedException {
        processamentoService.execute(new ProcessamentoRelatorioCache(relatorio, type));

        return ProcessamentoRelatorioCache.file(relatorio, type);
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EBD, Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_INSCRICAO_CULTO})
    public File exportaInscritos(Long id, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioInscritos(appService.buscaEvento(id),
                daoService.find(Template.class, sessaoBean.getChaveIgreja())), tipo);
    }

    @Override
    @AllowAdmin({Funcionalidade.MANTER_EBD, Funcionalidade.MANTER_EVENTOS, Funcionalidade.MANTER_INSCRICAO_CULTO})
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
                daoService.find(Template.class, sessaoBean.getChaveIgreja()),
                entidade), tipo);
    }

    @Override
    public File exportaEstudo(Long estudo, String tipo) throws IOException, InterruptedException {
        Estudo entidade = appService.buscaEstudo(estudo);
        return export(new RelatorioEstudo(entidade,
                daoService.find(Template.class, sessaoBean.getChaveIgreja())), tipo);
    }

    @Override
    @AllowAdmin(Funcionalidade.MANTER_VOTACOES)
    public File exportaResultadosVotacao(Long votacao, String tipo) throws IOException, InterruptedException {
        return export(new RelatorioResultadoVotacao(appService.buscaResultado(votacao),
                daoService.find(Template.class, sessaoBean.getChaveIgreja())), tipo);
    }
}
