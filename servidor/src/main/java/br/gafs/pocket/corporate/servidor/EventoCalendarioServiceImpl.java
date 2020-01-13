package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.dto.BuscaPaginadaEventosCalendarioDTO;
import br.gafs.pocket.corporate.dto.EventoCalendarioDTO;
import br.gafs.pocket.corporate.entity.EventoCalendario;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.pocket.corporate.servidor.google.GoogleService;
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

    @Schedule(hour = "*", minute = "0/15", persistent = false)
    public void atualizaEventosCalendario() {
        LOGGER.info("Iniciando atualização de eventos de calendário.");

        List<Empresa> empresas = daoService.findWith(QueryAdmin.EMPRESAS_ATIVAS.create());

        LOGGER.info(empresas.size() + " empresas encontradas atualiza eventos de calendário.");

        Date dataMaxima = DateUtil.incrementaAnos(new Date(), 2);
        for (Empresa empresa : empresas) {
            List<String> calendarIds = paramService.get(empresa.getChave(), TipoParametro.GOOGLE_CALENDAR_ID);

            if (calendarIds != null && !calendarIds.isEmpty()) {
                LOGGER.info("Sincronizando calendário para empresa " + empresa.getChave());

                Date limite = new Date();

                try {
                    for (String calendarId : calendarIds) {
                        String nextPage = null;

                        do {
                            BuscaPaginadaEventosCalendarioDTO busca = googleService
                                    .buscaEventosCalendar(empresa.getChave(), calendarId, nextPage, 50);

                            userTransaction.begin();

                            for (EventoCalendarioDTO evento : busca.getEventos()) {
                                daoService.update(new EventoCalendario(
                                        empresa, evento.getId(), evento.getInicio(),
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
                    }

                    userTransaction.begin();

                    daoService.execute(QueryAdmin.REMOVE_EVENTO_CALENDARIO_POR_EMPRESA
                            .create(empresa.getChave(), limite));

                    userTransaction.commit();

                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Falha ao sincronizar calendários", ex);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e) {
                    }
                }

                LOGGER.info("Calendários para empresa " + empresa.getChave() + " sincronizados.");
            }

        }
    }

}

