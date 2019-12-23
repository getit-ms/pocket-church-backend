/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.FiltroEmail;
import br.gafs.calvinista.dao.QueryNotificacao;
import br.gafs.calvinista.dto.*;
import br.gafs.calvinista.entity.NotificationSchedule;
import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.SentNotification;
import br.gafs.calvinista.entity.domain.NotificationType;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.mensagem.EmailService;
import br.gafs.calvinista.servidor.mensagem.NotificacaoService;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 *
 * @author Gabriel
 */
@Singleton
public class MensagemServiceImpl implements MensagemService {

    public static final String ASSUNTO_FIXO_APP_IPB = "IPB App - Interesse Pocket Church";

    @EJB
    private DAOService daoService;

    @EJB
    private EmailService emailService;

    @EJB
    private ParametroService paramService;

    @EJB
    private NotificacaoService notificacaoService;

    @EJB
    private NotificationScheduleServiceImpl notificationScheduleService;

    @Inject
    private SessaoBean sessaoBean;

    private ObjectMapper om = new ObjectMapper();

    private static List<String> DISPOSITIVOS_LIDOS = new ArrayList<String>();
    private static List<RegistroIgrejaId> MEMBROS_LIDOS = new ArrayList<RegistroIgrejaId>();

    @Schedule(minute = "*/5", hour = "*", persistent = false)
    public void enviaPushAgendados(){
        List<NotificationSchedule> notificacoes = daoService.
                findWith(QueryNotificacao.NOTIFICACOES_A_EXECUTAR.create(NotificationType.PUSH));

        for (NotificationSchedule notificacao : notificacoes){
            sendPushNow(notificacao);
        }
    }

    @Override
    public void enviarMensagem(ContatoDTO contato) {
        if (ASSUNTO_FIXO_APP_IPB.equals(contato.getAssunto())) {

            EmailUtil.sendMail(
                    MessageFormat.format(
                            (String) paramService.get(Parametro.GLOBAL, TipoParametro.EMAIL_BODY_PC_IPB_RESPOSTA),
                            contato.getNome()
                    ),
                    (String) paramService.get(Parametro.GLOBAL, TipoParametro.EMAIL_SUBJECT_PC_IPB_RESPOSTA),
                    contato.getEmail()
            );

        }

        EmailUtil.alertAdm(
                MessageFormat.format(
                        (String) paramService.get(Parametro.GLOBAL, TipoParametro.EMAIL_BODY_CONTATO_SITE_RESPOSTA),
                        contato.getMensagem(), contato.getNome(), contato.getEmail(), contato.getTelefone()
                ),
                MessageFormat.format(
                        (String) paramService.get(Parametro.GLOBAL, TipoParametro.EMAIL_SUBJECT_CONTATO_SITE_RESPOSTA),
                        contato.getAssunto()
                )
        );
    }

    private void sendPushNow(final NotificationSchedule notificacao){
        try{
            sendNow(notificacao, FiltroDispositivoNotificacaoDTO.class, MensagemPushDTO.class, new Sender<FiltroDispositivoNotificacaoDTO, MensagemPushDTO>() {
                @Override
                public void send(FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO t) throws IOException {
                    notificacaoService.enviaNotificacoes(notificacao.getId(), filtro.clone(), t.clone());
                }

            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Schedule(hour = "*", minute = "0/5", persistent = false)
    public void flushNotificacoesLidas(){
        {
            Set<String> flush = new HashSet<String>();
            synchronized (DISPOSITIVOS_LIDOS){
                flush.addAll(DISPOSITIVOS_LIDOS);
                DISPOSITIVOS_LIDOS.clear();
            }


            for (String dispositivo : flush){
                List<SentNotification> sns = daoService.findWith(QueryNotificacao.
                        NOTIFICACOES_NAO_LIDAS_DISPOSITIVO.create(dispositivo));
                for (SentNotification sn : sns){
                    sn.lido();
                    daoService.update(sn);
                }
            }
        }

        {
            Set<RegistroIgrejaId> flush = new HashSet<RegistroIgrejaId>();
            synchronized (MEMBROS_LIDOS){
                flush.addAll(MEMBROS_LIDOS);
                MEMBROS_LIDOS.clear();
            }


            for (RegistroIgrejaId membro : flush){
                List<SentNotification> sns = daoService.findWith(QueryNotificacao.NOTIFICACOES_NAO_LIDAS_MEMBRO.
                        create(membro.getChaveIgreja(), membro.getId()));
                for (SentNotification sn : sns){
                    sn.lido();
                    daoService.update(sn);
                }
            }
        }
    }

    @Override
    @Asynchronous
    public void marcaNotificacoesComoLidas(String chaveIgreja, String chaveDispositivo, Long idMembro) {
        synchronized (DISPOSITIVOS_LIDOS){
            DISPOSITIVOS_LIDOS.add(chaveDispositivo);
        }

        if (idMembro != null){
            synchronized (MEMBROS_LIDOS){
                MEMBROS_LIDOS.add(new RegistroIgrejaId(chaveIgreja, idMembro));
            }
        }
    }

    @Override
    public Long countNotificacoesNaoLidas() {
        return countNotificacoesNaoLidas(
                sessaoBean.getChaveIgreja(),
                sessaoBean.getChaveDispositivo(),
                sessaoBean.getIdMembro()
        );
    }

    @Override
    public Long countNotificacoesNaoLidas(String chaveIgreja, String chaveDispositivo, Long idMembro) {

        if (idMembro != null){
            synchronized (MEMBROS_LIDOS){
                if (MEMBROS_LIDOS.contains(new RegistroIgrejaId(chaveIgreja, idMembro))){
                    return 0l;
                }
            }
            return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS_MEMBRO.
                    createSingle(chaveIgreja, idMembro));
        }

        synchronized (DISPOSITIVOS_LIDOS){
            if (DISPOSITIVOS_LIDOS.contains(chaveDispositivo)){
                return 0l;
            }
        }

        return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS_DISPOSITIVO.
                createSingle(chaveIgreja, chaveDispositivo));
    }

    @Schedule(minute = "*/5", hour = "*", persistent = false)
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
