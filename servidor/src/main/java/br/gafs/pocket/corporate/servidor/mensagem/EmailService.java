/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor.mensagem;

import br.gafs.pocket.corporate.dto.MensagemEmailDTO;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.util.ConfiguradorEmailService;
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

    public void sendEmails(final Empresa empresa, MensagemEmailDTO t, List to) {
        EmailSender sender = new EmailSender(new ConfiguradorEmpresa(empresa));

        try {
            sender.sendMailWithDataSources(t.getMessage(), t.getSubject(),
                    to, t.getDataSources(), t.getAttachmentsNames());
        } catch (EmailException ex) {
            Logger.getLogger(EmailService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @RequiredArgsConstructor
    class ConfiguradorEmpresa implements ConfiguradorSMTP {
        private final Empresa empresa;

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
            return empresa.getNome();
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
