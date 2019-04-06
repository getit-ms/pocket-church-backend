package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dto.BuscaPaginadaEventosCalendarioDTO;
import br.gafs.calvinista.dto.EventoCalendarioDTO;
import br.gafs.calvinista.entity.EventoCalendario;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.google.GoogleService;
import br.gafs.dao.DAOService;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class EventoCalendarioServiceImpl {
    private static final Logger LOGGER = Logger.getLogger(EventoCalendarioServiceImpl.class.getName());

    @EJB
    private DAOService daoService;

    @EJB
    private ParametroService paramService;

    @EJB
    private GoogleService googleService;

    @Resource
    private UserTransaction userTransaction;

    @Schedule(hour = "*", minute = "0/5")
    public void atualizaEventosCalendario() {
        LOGGER.info("Iniciando atualização de eventos de calendário.");

        List<Igreja> igrejas = daoService.findWith(QueryAdmin.IGREJAS_ATIVAS.create());

        LOGGER.info(igrejas.size() + " igrejas encontradas atualiza eventos de calendário.");

        Date dataMaxima = DateUtil.incrementaAnos(new Date(), 2);
        for (Igreja igreja : igrejas) {
            List<String> calendarIds = paramService.get(igreja.getChave(), TipoParametro.GOOGLE_CALENDAR_ID);

            if (calendarIds != null && !calendarIds.isEmpty()) {
                LOGGER.info("Sincronizando calendário para igreja " + igreja.getChave());

                for (String calendarId : calendarIds) {
                    String nextPage = null;

                    Date limite = new Date();

                    try {

                        do {
                            BuscaPaginadaEventosCalendarioDTO busca = googleService
                                    .buscaEventosCalendar(igreja.getChave(), calendarId, nextPage, 50);

                            userTransaction.begin();

                            for (EventoCalendarioDTO evento : busca.getEventos()) {
                                daoService.update(new EventoCalendario(
                                        igreja, evento.getId(), evento.getInicio(),
                                        evento.getTermino(), evento.getDescricao(), evento.getLocal()
                                ));
                            }

                            userTransaction.commit();

                            if (busca.isPossuiProximaPagina() && (busca.getEventos().isEmpty() ||
                                    busca.getEventos().get(busca.getEventos().size() - 1)
                                            .getInicio().before(dataMaxima))) {
                                nextPage = busca.getProximaPagina();
                            } else {
                                nextPage = null;
                            }

                        } while (nextPage != null);

                        userTransaction.begin();

                        daoService.execute(QueryAdmin.REMOVE_EVENTO_CALENDARIO_POR_IGREJA
                                .create(igreja.getChave(), limite));

                        userTransaction.commit();

                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Falha ao sincronizar calendários", ex);

                        try {
                            userTransaction.rollback();
                        } catch (SystemException e) {
                        }
                    }
                }

                LOGGER.info("Calendários para igreja " + igreja.getChave() + " sincronizados.");
            }

        }
    }

}
