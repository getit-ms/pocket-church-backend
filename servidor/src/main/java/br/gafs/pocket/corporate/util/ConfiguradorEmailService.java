package br.gafs.pocket.corporate.util;

import br.gafs.pocket.corporate.dto.ParametrosGlobaisDTO;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.util.email.ConfiguradorSMTP;
import br.gafs.util.email.EmailUtil;

import javax.annotation.PostConstruct;
import javax.ejb.*;
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

    private ParametrosGlobaisDTO parametros;

    @PostConstruct
    public void prepare() {
        carregaDados();

        EmailUtil.setConfigurador(this);
    }

    @Schedule(hour = "*", minute = "*/30")
    public void carregaDados() {
        this.parametros = paramService.buscaParametrosGlobais();
    }

    @Override
    public Integer getSmtpPort() {
        return parametros.getSmtpPort();
    }

    @Override
    public Boolean getEnableStartTls() {
        return parametros.getEnableStartTls();
    }

    @Override
    public boolean isAuth() {
        return parametros.getAuth();
    }

    @Override
    public Properties getProperties() {
        Properties prop = new Properties();

        try {
            prop.load(new ByteArrayInputStream(parametros.getProperties().getBytes()));
        } catch (IOException e) {
            Logger.getLogger(ConfiguradorEmailService.class.getName())
                    .log(Level.SEVERE, "Erro ao recuperar properties de SMTP", e);
        }

        return prop;
    }

    @Override
    public List<String> getAdminMails() {
        return Arrays.asList((parametros.getAdminMails()).split("\\s*,\\s*"));
    }

    @Override
    public String getUsername() {
        return parametros.getUsername();
    }

    @Override
    public String getPassword() {
        return parametros.getPassword();
    }

    @Override
    public String getFromName() {
        return parametros.getFromName();
    }

    @Override
    public String getFromEmail() {
        return parametros.getFromEmail();
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

}

