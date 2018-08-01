/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.dao.FiltroEmail;
import br.gafs.pocket.corporate.dao.QueryNotificacao;
import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.dto.*;
import br.gafs.pocket.corporate.entity.NotificationSchedule;
import br.gafs.pocket.corporate.entity.RegistroEmpresaId;
import br.gafs.pocket.corporate.entity.SentNotification;
import br.gafs.pocket.corporate.entity.domain.NotificationType;
import br.gafs.pocket.corporate.service.MensagemService;
import br.gafs.pocket.corporate.servidor.mensagem.EmailService;
import br.gafs.pocket.corporate.servidor.mensagem.NotificacaoService;
import br.gafs.pocket.corporate.util.MensagemUtil;
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
import java.util.*;

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

    @Inject
    private SessaoBean sessaoBean;

    private ObjectMapper om = new ObjectMapper();

    private static List<String> DISPOSITIVOS_LIDOS = new ArrayList<String>();
    private static List<RegistroEmpresaId> COLABORADORS_LIDOS = new ArrayList<RegistroEmpresaId>();

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
                    notificacaoService.enviaNotificacoes(notificacao.getId(), filtro.clone(), t.clone());
                }

            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Schedule(hour = "*", minute = "0/5")
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
            Set<RegistroEmpresaId> flush = new HashSet<RegistroEmpresaId>();
            synchronized (COLABORADORS_LIDOS){
                flush.addAll(COLABORADORS_LIDOS);
                COLABORADORS_LIDOS.clear();
            }


            for (RegistroEmpresaId colaborador : flush){
                List<SentNotification> sns = daoService.findWith(QueryNotificacao.NOTIFICACOES_NAO_LIDAS_COLABORADOR.
                        create(colaborador.getChaveEmpresa(), colaborador.getId()));
                for (SentNotification sn : sns){
                    sn.lido();
                    daoService.update(sn);
                }
            }
        }
    }

    @Override
    @Asynchronous
    public void marcaNotificacoesComoLidas(String chaveEmpresa, String chaveDispositivo, Long idColaborador) {
        synchronized (DISPOSITIVOS_LIDOS){
            DISPOSITIVOS_LIDOS.add(chaveDispositivo);
        }

        if (idColaborador != null){
            synchronized (COLABORADORS_LIDOS){
                COLABORADORS_LIDOS.add(new RegistroEmpresaId(chaveEmpresa, idColaborador));
            }
        }
    }

    @Override
    public Long countNotificacoesNaoLidas() {
        return countNotificacoesNaoLidas(
                sessaoBean.getChaveEmpresa(),
                sessaoBean.getChaveDispositivo(),
                sessaoBean.getIdColaborador()
        );
    }

    @Override
    public Long countNotificacoesNaoLidas(String chaveEmpresa, String chaveDispositivo, Long idColaborador) {

        if (idColaborador != null){
            synchronized (COLABORADORS_LIDOS){
                if (COLABORADORS_LIDOS.contains(new RegistroEmpresaId(chaveEmpresa, idColaborador))){
                    return 0l;
                }
            }
            return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS_COLABORADOR.
                    createSingle(chaveEmpresa, idColaborador));
        }

        synchronized (DISPOSITIVOS_LIDOS){
            if (DISPOSITIVOS_LIDOS.contains(chaveDispositivo)){
                return 0l;
            }
        }

        return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS_DISPOSITIVO.
                createSingle(chaveEmpresa, chaveDispositivo));
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

                        emailService.sendEmails(filtro.getEmpresa(), t, emails.getResultados());

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
