/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryNotificacao;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.NotificationSchedule;
import br.gafs.calvinista.entity.domain.NotificationType;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.servidor.mensagem.AndroidNotificationService;
import br.gafs.calvinista.servidor.mensagem.EmailService;
import br.gafs.calvinista.servidor.mensagem.IOSNotificationService;
import br.gafs.dao.DAOService;
import br.gafs.util.date.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
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
    private IOSNotificationService iOSNotificationService;
    
    @EJB
    private EmailService emailService;
    
    @EJB
    private AndroidNotificationService androidNotificationService;
    
    private ObjectMapper om = new ObjectMapper();
    
    @Schedule(minute = "*/5", hour = "*")
    public void enviaPushAgendados(){
        List<NotificationSchedule> notificacoes = daoService.
                findWith(QueryNotificacao.NOTIFICACOES_A_EXECUTAR.create(NotificationType.PUSH));
        
        for (NotificationSchedule notificacao : notificacoes){
            sendPushNow(notificacao);
        }
    }
    
    private void sendPushNow(NotificationSchedule notificacao){
            try{
                sendNow(notificacao, MensagemPushDTO.class, new Sender<MensagemPushDTO>() {

                    @Override
                    public void send(Igreja igreja, MensagemPushDTO t, List to) throws IOException {
                        List<String> android = separa(igreja, to, TipoDispositivo.ANDROID);
                        if (!android.isEmpty()){
                            androidNotificationService.pushNotifications(igreja, t, android);
                        }

                        List<String> ios = separa(igreja, to, TipoDispositivo.IPHONE);
                        if (!ios.isEmpty()){
                            iOSNotificationService.pushNotifications(igreja, t, ios);
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
                sendNow(notificacao, MensagemEmailDTO.class, new Sender<MensagemEmailDTO>() {

                    @Override
                    public void send(Igreja igreja, MensagemEmailDTO t, List to) throws IOException {
                        emailService.sendEmails(igreja, t, to);
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
    }
    
    private <T> void sendNow(NotificationSchedule notificacao, Class<T> type, Sender<T> sender) throws IOException {
        T t = om.readValue(notificacao.getNotificacao(), type);
        List<String> tos = om.readValue(notificacao.getTo(), List.class);
        
        sender.send(notificacao.getIgreja(), t, tos);
            
        notificacao.enviado();
        daoService.update(notificacao);
    }
    
    interface Sender<T> {
        void send(Igreja igreja, T t, List<String> to) throws IOException;
    }
    
    @Override
    @Asynchronous
    public void sendNow(Igreja igreja, MensagemPushDTO notificacao, List<String> tos) {
        if (tos.isEmpty()) return;
        
        sendPushNow(sendWhenPossible(igreja, notificacao, tos));
    }
    
    @Override
    @Asynchronous
    public void sendNow(Igreja igreja, MensagemEmailDTO notificacao, List<String> tos) {
        if (tos.isEmpty()) return;
        
        sendEmailNow(sendWhenPossible(igreja, notificacao, tos));
    }

    @Override
    public NotificationSchedule sendWhenPossible(Igreja igreja, MensagemPushDTO notificacao, List<String> to) {
        return sendLater(igreja, notificacao, to, DateUtil.getDataAtual());
    }
    
    @Override
    public NotificationSchedule sendWhenPossible(Igreja igreja, MensagemEmailDTO notificacao, List<String> to) {
        return sendLater(igreja, notificacao, to, DateUtil.getDataAtual());
    }
    
    private List<String> separa(Igreja igreja, List<String> devices, TipoDispositivo tipo){
        List<String> filtrados = new ArrayList<String>();
        for (int i=0;i<devices.size();i+=500){
            filtrados.addAll(daoService.findWith(QueryNotificacao.DEVICES_POR_TIPO.create(igreja.getChave(), 
                    tipo, devices.subList(i, Math.min(devices.size(), i + 500)))));
        }
        return filtrados;
    }
    
    @Override
    public NotificationSchedule sendLater(Igreja igreja, MensagemPushDTO notificacao, List<String> to, Date dataHora) {
        try {
            NotificationSchedule registro = new NotificationSchedule(
                    NotificationType.PUSH,
                    dataHora, igreja,
                    om.writeValueAsString(notificacao),
                    om.writeValueAsString(to));
            
            return daoService.create(registro);
        } catch (IOException ex) {
            Logger.getLogger(MensagemServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @Override
    public NotificationSchedule sendLater(Igreja igreja, MensagemEmailDTO notificacao, List<String> to, Date dataHora) {
        try {
            NotificationSchedule registro = new NotificationSchedule(
                    NotificationType.EMAIL,
                    dataHora, igreja,
                    om.writeValueAsString(notificacao),
                    om.writeValueAsString(to));
            
            return daoService.create(registro);
        } catch (IOException ex) {
            Logger.getLogger(MensagemServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
