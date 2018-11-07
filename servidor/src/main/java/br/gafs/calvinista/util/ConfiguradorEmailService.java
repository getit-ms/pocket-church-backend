package br.gafs.calvinista.util;

import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.util.email.ConfiguradorSMTP;
import br.gafs.util.email.EmailUtil;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 25/10/2018.
 */
@Startup
@Singleton
@LocalBean
public class ConfiguradorEmailService implements ConfiguradorSMTP {

    @EJB
    private ParametroService paramService;

    @PostConstruct
    public void prepare() {
        EmailUtil.setConfigurador(this);
    }

    @Override
    public Integer getSmtpPort() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_PORTA);
    }

    @Override
    public Boolean getEnableStartTls() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_ENABLE_START_TLS);
    }

    @Override
    public boolean isAuth() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_AUTH);
    }

    @Override
    public Properties getProperties() {
        Properties prop = new Properties();

        try {
            prop.load(new ByteArrayInputStream(((String) paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_PROPERTIES)).getBytes()));
        } catch (IOException e) {
            Logger.getLogger(ConfiguradorEmailService.class.getName())
                    .log(Level.SEVERE, "Erro ao recuperar properties de SMTP", e);
        }

        return prop;
    }

    @Override
    public List<String> getAdminMails() {
        return Arrays.asList(((String) paramService.get(Parametro.GLOBAL, TipoParametro.ADMIN_MAILS)).split("\\s*,\\s*"));
    }

    @Override
    public String getUsername() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_USERNAME);
    }

    @Override
    public String getPassword() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_PASSWORD);
    }

    @Override
    public String getFromName() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_FROM_NAME);
    }

    @Override
    public String getFromEmail() {
        return paramService.get(Parametro.GLOBAL, TipoParametro.SMTP_FROM_EMAIL);
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }
}
