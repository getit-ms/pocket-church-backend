package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.pocket.corporate.dto.FiltroEmailDTO;
import br.gafs.pocket.corporate.dto.MensagemEmailDTO;
import br.gafs.pocket.corporate.dto.MensagemPushDTO;
import br.gafs.pocket.corporate.entity.NotificationSchedule;
import br.gafs.pocket.corporate.entity.domain.NotificationType;
import br.gafs.pocket.corporate.view.View;
import br.gafs.dao.DAOService;
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
    private DAOService daoService;

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
