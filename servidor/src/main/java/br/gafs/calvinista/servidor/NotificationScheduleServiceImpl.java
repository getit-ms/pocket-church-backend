package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.FiltroEmailDTO;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.NotificationSchedule;
import br.gafs.calvinista.entity.domain.NotificationType;
import br.gafs.calvinista.view.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 16/11/2017.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class NotificationScheduleServiceImpl {

    @EJB
    private CustomDAOService daoService;

    private ObjectMapper om = new ObjectMapper();

    public NotificationSchedule createPUSH(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro, Date dataHora) {
        try {

            ObjectWriter writer = om.writerWithView(View.Resumido.class);

            NotificationSchedule registro = new NotificationSchedule(
                    NotificationType.PUSH, dataHora,
                    writer.writeValueAsString(notificacao),
                    writer.writeValueAsString(filtro));

            return daoService.create(registro);
        } catch (IOException ex) {
            Logger.getLogger(MensagemServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public NotificationSchedule createEMAIL(MensagemEmailDTO notificacao, FiltroEmailDTO filtro, Date dataHora) {
        try {
            ObjectWriter writer = om.writerWithView(View.Resumido.class);

            NotificationSchedule registro = new NotificationSchedule(
                    NotificationType.EMAIL, dataHora,
                    writer.writeValueAsString(notificacao),
                    writer.writeValueAsString(filtro));

            return daoService.create(registro);
        } catch (IOException ex) {
            Logger.getLogger(MensagemServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
