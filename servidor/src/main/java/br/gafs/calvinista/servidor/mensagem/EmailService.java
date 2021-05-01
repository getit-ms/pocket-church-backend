/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.util.ConfiguradorEmailService;
import br.gafs.util.email.ConfiguradorSMTP;
import br.gafs.util.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.apache.commons.mail.EmailException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@Stateless
public class EmailService {

    @EJB
    private ConfiguradorEmailService configuracdor;

    public void sendEmails(final Igreja igreja, MensagemEmailDTO t, List to) {
        EmailSender sender = new EmailSender(new ConfiguradorIgreja(igreja));
        
        try {
            sender.sendMailWithDataSources(t.getMessage(), t.getSubject(),
                    to, t.getDataSources(), t.getAttachmentsNames());
        } catch (EmailException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @RequiredArgsConstructor
    class ConfiguradorIgreja implements ConfiguradorSMTP {
        private final Igreja igreja;

        @Override
        public Integer getSmtpPort() {
            return configuracdor.getSmtpPort();
        }

        @Override
        public Boolean getEnableStartTls() {
            return configuracdor.getEnableStartTls();
        }

        @Override
        public boolean isAuth() {
            return configuracdor.isAuth();
        }

        @Override
        public Properties getProperties() {
            return configuracdor.getProperties();
        }

        @Override
        public List<String> getAdminMails() {
            return configuracdor.getAdminMails();
        }

        @Override
        public String getUsername() {
            return configuracdor.getUsername();
        }

        @Override
        public String getPassword() {
            return configuracdor.getPassword();
        }

        @Override
        public String getFromName() {
            return igreja.getNome();
        }

        @Override
        public String getFromEmail() {
            return configuracdor.getFromEmail();
        }

        @Override
        public void start() {

        }

        @Override
        public void end() {

        }
    }
}
