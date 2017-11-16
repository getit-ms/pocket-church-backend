/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dao.FiltroEmail;
import br.gafs.calvinista.dao.QueryNotificacao;
import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.NotificationSchedule;
import br.gafs.calvinista.entity.domain.NotificationType;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.servidor.mensagem.EmailService;
import br.gafs.calvinista.servidor.mensagem.NotificacaoService;
import br.gafs.calvinista.util.MensagemUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Singleton
public class MensagemServiceImpl implements MensagemService {

    @EJB
    private DAOService daoService;

    @EJB
    private EmailService emailService;

    @EJB
    private NotificacaoService notificacaoService;

    @EJB
    private NotificationScheduleServiceImpl notificationScheduleService;

    private ObjectMapper om = new ObjectMapper();

    @Schedule(minute = "*/5", hour = "*")
    public void enviaPushAgendados(){
        List<NotificationSchedule> notificacoes = daoService.
                findWith(QueryNotificacao.NOTIFICACOES_A_EXECUTAR.create(NotificationType.PUSH));

        for (NotificationSchedule notificacao : notificacoes){
            sendPushNow(notificacao);
        }
    }

    @Override
    public void enviarMensagem(ContatoDTO contato) {
        EmailUtil.alertAdm(
                MensagemUtil.getMensagem("email.contato.message", "pt-br",
                        contato.getMensagem(), contato.getNome(), contato.getEmail(), contato.getTelefone()),
                MensagemUtil.getMensagem("email.contato.subject", "pt-br", contato.getAssunto()));
    }

    private void sendPushNow(final NotificationSchedule notificacao){
        try{
            sendNow(notificacao, FiltroDispositivoNotificacaoDTO.class, MensagemPushDTO.class, new Sender<FiltroDispositivoNotificacaoDTO, MensagemPushDTO>() {
                @Override
                public void send(FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO t) throws IOException {
                    FiltroDispositivoNotificacao filtroDispositivoNotificacao = new FiltroDispositivoNotificacao(filtro);
                    Long count = daoService.findWith(filtroDispositivoNotificacao.getCountQuery());

                    for (int i=0;i<count;i+=filtroDispositivoNotificacao.getResultLimit()) {
                        try {
                            notificacaoService.enviaNotificacoes(notificacao.getId(), filtro.clone(), t.clone());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        filtro.proxima();
                    }

                }

            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Schedule(minute = "*/5", hour = "*")
    public void enviaEmailsAgendados(){
        List<NotificationSchedule> notificacoes = daoService.
                findWith(QueryNotificacao.NOTIFICACOES_A_EXECUTAR.create(NotificationType.EMAIL));

        for (NotificationSchedule notificacao : notificacoes){
            sendEmailNow(notificacao);
        }
    }

    private void sendEmailNow(NotificationSchedule notificacao){
        try{
            sendNow(notificacao, FiltroEmailDTO.class, MensagemEmailDTO.class, new Sender<FiltroEmailDTO, MensagemEmailDTO>() {

                @Override
                public void send(FiltroEmailDTO filtro, MensagemEmailDTO t) throws IOException {
                    BuscaPaginadaDTO<String> emails;
                    do{
                        emails = daoService.findWith(new FiltroEmail(filtro));

                        emailService.sendEmails(filtro.getIgreja(), t, emails.getResultados());

                        filtro.proxima();
                    }while(emails.isHasProxima());
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private <F,M> void sendNow(NotificationSchedule notificacao, Class<F> ftype, Class<M> mtype, Sender<F, M> sender) throws IOException {
        M t = om.readValue(notificacao.getNotificacao(), mtype);
        F filtro = om.readValue(notificacao.getTo(), ftype);

        sender.send(filtro, t);

        notificacao.enviado();

        daoService.update(notificacao);
    }

    interface Sender<F,M> {
        void send(F filtro, M t) throws IOException;
    }

    @Override
    @Asynchronous
    public void sendNow(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro) {
        sendPushNow(sendWhenPossible(notificacao, filtro));
    }

    @Override
    @Asynchronous
    public void sendNow(MensagemEmailDTO notificacao, FiltroEmailDTO filtro) {
        sendEmailNow(sendWhenPossible(notificacao, filtro));
    }

    @Override
    public NotificationSchedule sendWhenPossible(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro) {
        return sendLater(notificacao, filtro, DateUtil.getDataAtual());
    }

    @Override
    public NotificationSchedule sendWhenPossible(MensagemEmailDTO notificacao, FiltroEmailDTO filtro) {
        return sendLater(notificacao, filtro, DateUtil.getDataAtual());
    }

    @Override
    public NotificationSchedule sendLater(MensagemPushDTO notificacao, FiltroDispositivoNotificacaoDTO filtro, Date dataHora) {
        return notificationScheduleService.createPUSH(notificacao, filtro, dataHora);
    }

    @Override
    public NotificationSchedule sendLater(MensagemEmailDTO notificacao, FiltroEmailDTO filtro, Date dataHora) {
        return notificationScheduleService.createEMAIL(notificacao, filtro, dataHora);
    }

}
