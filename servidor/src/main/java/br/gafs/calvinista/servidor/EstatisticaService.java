package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.EstatisticaAcesso;
import br.gafs.calvinista.entity.EstatisticaDispositivo;
import br.gafs.calvinista.entity.RegistroAcesso;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.StatusRegistroAcesso;
import br.gafs.dao.DAOService;

import javax.ejb.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Startup
@Singleton
public class EstatisticaService {
    private static final Logger LOGGER = Logger.getLogger(EventoCalendarioServiceImpl.class.getName());

    @EJB
    private DAOService daoService;

    @Schedule(hour = "*", minute = "*/30")
    public void registraEstatisticasDispositivosIgrejas() {
        LOGGER.info("Iniciando registro de estatísticas de dispositivos.");

        List<EstatisticaDispositivo> estatisticas = daoService.findWith(QueryAdmin.ESTATISTICAS_DISPOSITIVOS_ONLINE.create());

        for (EstatisticaDispositivo e : estatisticas) {
            daoService.update(e);
        }

        LOGGER.info("Estatísticas de dispositivos registradas com sucesso.");
    }

    @Schedule(hour = "*", minute = "*/30")
    public void registraEstatisticasAcessoIgrejas() {
        LOGGER.info("Iniciando registro de estatísticas de acesso.");

        List<EstatisticaAcesso> estatisticas = daoService.findWith(QueryAdmin.ESTATISTICAS_ACESSO_ONLINE.create());

        for (EstatisticaAcesso e : estatisticas) {
            daoService.update(e);
        }

        LOGGER.info("Estatísticas de acesso registradas com sucesso.");
    }

    @Schedule(hour = "*")
    public void removeEstatisticasDisositivosIgrejasAntigas() {
        LOGGER.info("Iniciando remoção de estatísticas de dispositivos antigas.");

        daoService.execute(QueryAdmin.REMOVE_ESTATISTICAS_DISPOSITIVOS_IGREJAS_ANTIGAS.create());

        LOGGER.info("Estatísticas de dispositivos antigas removidas com sucesso.");
    }

    @Schedule(hour = "*")
    public void removeEstatisticasAcessoIgrejasAntigas() {
        LOGGER.info("Iniciando remoção de estatísticas de acesso antigas.");

        daoService.execute(QueryAdmin.REMOVE_ESTATISTICAS_ACESSO_IGREJAS_ANTIGAS.create());

        LOGGER.info("Estatísticas de acesso antigas removidas com sucesso.");
    }

    @Schedule(hour = "*")
    public void removeRegistrosAcessoIgrejasAntigas() {
        LOGGER.info("Iniciando remoção de registros de acesso antigas.");

        daoService.execute(QueryAdmin.REMOVE_REGISTROS_ACESSO_IGREJAS_ANTIGAS.create());

        LOGGER.info("Registros de acesso antigas removidas com sucesso.");
    }

    @Asynchronous
    public void registraAcesso(
            Funcionalidade funcionalidade,
            String dispositivo,
            String igreja,
            StatusRegistroAcesso status
    ) {
        daoService.create(new RegistroAcesso(new Date(), funcionalidade.getCodigo(), dispositivo, igreja, status));
    }

}
