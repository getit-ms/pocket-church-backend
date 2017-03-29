/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dao.FiltroEmail;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dao.QueryNotificacao;
import br.gafs.calvinista.dao.RegisterSentNotifications;
import br.gafs.calvinista.dto.ContatoDTO;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.FiltroEmailDTO;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.NotificationSchedule;
import br.gafs.calvinista.entity.domain.NotificationType;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.servidor.mensagem.AndroidNotificationService;
import br.gafs.calvinista.servidor.mensagem.EmailService;
import br.gafs.calvinista.servidor.mensagem.IOSNotificationService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoNotificacaoAndroid;
import br.gafs.calvinista.servidor.processamento.ProcessamentoNotificacaoIOS;
import br.gafs.calvinista.util.MensagemUtil;
import br.gafs.calvinista.view.View.Resumido;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

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
    private ProcessamentoService processamentoService;
    
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
                        contato.getMensagem(), contato.getNome(), contato.getEmail()),
                MensagemUtil.getMensagem("email.contato.subject", "pt-br", contato.getAssunto()));
    }
    
    private void sendPushNow(final NotificationSchedule notificacao){
        try{
            sendNow(notificacao, FiltroDispositivoNotificacaoDTO.class, MensagemPushDTO.class, new Sender<FiltroDispositivoNotificacaoDTO, MensagemPushDTO>() {

                @Override
                public void send(FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO t) throws IOException {
                    try {
                        processamentoService.schedule(new ProcessamentoNotificacaoAndroid(notificacao.getId(), filtro.clone(), t.clone()));
                    } catch (Exception ex) {
                        Logger.getLogger(MensagemServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    try {
                        processamentoService.schedule(new ProcessamentoNotificacaoIOS(notificacao.getId(), filtro.clone(), t.clone()));
                    } catch (Exception ex) {
                        Logger.getLogger(MensagemServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    daoService.execute(new RegisterSentNotifications(notificacao, filtro));
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
        try {
            ObjectWriter writer = om.writerWithView(Resumido.class);
            
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
    
    @Override
    public NotificationSchedule sendLater(MensagemEmailDTO notificacao, FiltroEmailDTO filtro, Date dataHora) {
        try {
            ObjectWriter writer = om.writerWithView(Resumido.class);
            
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
