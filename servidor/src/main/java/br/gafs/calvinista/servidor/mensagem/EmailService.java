/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.util.email.ConfiguradorSMTPPadrao;
import br.gafs.util.email.EmailSender;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author Gabriel
 */
@Stateless
public class EmailService {
    
    public void sendEmails(final Igreja igreja, MensagemEmailDTO t, List to) {
        EmailSender sender = new EmailSender(new ConfiguradorSMTPPadrao(){

            @Override
            public String getFromName() {
                return igreja.getNome();
            }
            
        });
        
        try {
            sender.sendMailWithDataSources(t.getMessage(), t.getSubject(),
                    to, t.getDataSources(), t.getAttachmentsNames());
        } catch (EmailException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
