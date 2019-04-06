package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Estatistica;
import br.gafs.dao.DAOService;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;
import java.util.logging.Logger;

@Startup
@Singleton
public class EstatisticaService {
    private static final Logger LOGGER = Logger.getLogger(EventoCalendarioServiceImpl.class.getName());

    @EJB
    private DAOService daoService;

    @Schedule(hour = "*", minute = "*/30")
    public void registraEstatisticasIgrejas() {
        LOGGER.info("Iniciando registro de estatísticas.");

        List<Estatistica> estatisticas = daoService.findWith(QueryAdmin.ESTATISTICAS_IGREJAS.create());

        for (Estatistica e : estatisticas) {
            daoService.update(e);
        }

        LOGGER.info("Estatísticas registradas com sucesso.");
    }


    @Schedule(hour = "*")
    public void removeEstatisticasIgrejasAntigas() {
        LOGGER.info("Iniciando remoção de estatísticas antigas.");

        daoService.execute(QueryAdmin.REMOVE_ESTATISTICAS_IGREJAS_ANTIGAS.create());

        LOGGER.info("Estatísticas anigas removidas com sucesso.");
    }

}
