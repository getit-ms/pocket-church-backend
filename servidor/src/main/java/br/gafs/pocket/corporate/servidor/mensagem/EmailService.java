/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor.mensagem;

import br.gafs.pocket.corporate.dto.MensagemEmailDTO;
import br.gafs.pocket.corporate.entity.Empresa;
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
    
    public void sendEmails(final Empresa empresa, MensagemEmailDTO t, List to) {
        EmailSender sender = new EmailSender(new ConfiguradorSMTPPadrao(){

            @Override
            public String getFromName() {
                return empresa.getNome();
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
