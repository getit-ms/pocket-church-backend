package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.dao.DAOService;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 07/02/2018.
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class DispositivoService {

    public static final Logger LOGGER = Logger.getLogger(DispositivoService.class.getName());

    private final Set<String> DISPOSITIVOS_ACESSADOS = new HashSet<>();

    private static final File CONTINGENCIA_DIR = new File("/calvin/contingencia");

    private static final String FORCE_REGISTER_MASK = "force_register_{0}";
    private static final String FULL_RESET_MASK = "full_reset_{0}";
    private static final String BIBLIA_RESET = "bible_reset_{0}";

    @EJB
    private DAOService daoService;

    @Resource
    private UserTransaction userTransaction;

    @Asynchronous
    public void registraPush(String chaveDispositivo, TipoDispositivo tipoDispositivo, String pushKey, String version) {
        LOGGER.info("Chamada para registro de dispositivo " + tipoDispositivo + " - " + pushKey + " - " + version);

        try {
            userTransaction.begin();

            LOGGER.info("Registro push para " + chaveDispositivo);

            Dispositivo dispositivo = getDispositivo(chaveDispositivo);

            LOGGER.info("Dispositivo " + chaveDispositivo + " existe. Registrando push " +
                    tipoDispositivo + "  - " + pushKey + " = " + version);

            dispositivo.registerToken(tipoDispositivo, pushKey, version);

            dispositivo = daoService.update(dispositivo);

            daoService.execute(QueryAcesso.UNREGISTER_OLD_DEVICES.create(dispositivo.getPushkey(), dispositivo.getChave()));

            userTransaction.commit();
        }catch(Exception ex0) {
            try {
                userTransaction.rollback();
            } catch (SystemException e) {}

            LOGGER.log(Level.SEVERE, "Dispositivo " + chaveDispositivo + " não pode ser registrado. Será removido da lista para não impedir novos registros", ex0);
        }
    }

    @Schedule(hour = "*", minute = "*")
    public void doFlushAcessos() {
        LOGGER.info("Iniciando flush de acessos de dispositivos");

        List<String> dispositivos;
        synchronized (DISPOSITIVOS_ACESSADOS) {
            dispositivos = new ArrayList<>(DISPOSITIVOS_ACESSADOS);
            DISPOSITIVOS_ACESSADOS.clear();
        }

        List<List<String>> paginas = new ArrayList<>();

        for (int i=0;i<dispositivos.size();i+=500) {
            paginas.add(dispositivos.subList(i, Math.min(i + 500, dispositivos.size())));
        }

        for (List<String> pag : paginas) {
            try {
                userTransaction.begin();

                daoService.execute(QueryAcesso.REGISTER_ACESSO_DISPOSITIVO.create(pag));

                userTransaction.commit();
            } catch (Exception ex) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e) {}

                LOGGER.log(Level.SEVERE, "Falha ao registrar o acesso de " + pag.size() + " dispositivos.", ex);
            }
        }

        LOGGER.info("Finalizando flush de acessos de " + dispositivos.size() + " dispositivos");
    }

    public static boolean shouldResetaBiblia(String chaveDispositivo) {
        return new File(CONTINGENCIA_DIR, MessageFormat.format(BIBLIA_RESET, chaveDispositivo)).exists();
    }

    public static void flagResetBibliaConcluido(String chaveDispositivo) {
        File flag = new File(CONTINGENCIA_DIR, MessageFormat.format(BIBLIA_RESET, chaveDispositivo));
        if (flag.exists()) {
            flag.delete();
        }
    }

    public static  TipoAcaoContigencia verificaContingencia(String chaveDispositivo) {
        File fullReset = new File(CONTINGENCIA_DIR, MessageFormat.format(FULL_RESET_MASK, chaveDispositivo));
        if (fullReset.exists()) {
            fullReset.delete();

            LOGGER.info("Envio de FULL RESET para dispositivo " + chaveDispositivo);

            return TipoAcaoContigencia.FULL_RESET;
        }

        File forcePush = new File(CONTINGENCIA_DIR, MessageFormat.format(FORCE_REGISTER_MASK, chaveDispositivo));
        if (forcePush.exists()) {
            forcePush.delete();

            LOGGER.info("Envio de FORCE REGISTER para dispositivo " + chaveDispositivo);

            return TipoAcaoContigencia.FORCE_REGISTER;
        }

        return TipoAcaoContigencia.NENHUMA;
    }

    private synchronized Dispositivo createDispositivo(String chaveDispositivo){
        String parts[] = chaveDispositivo.split("@");

        String uuid = parts[0];
        String chaveIgreja = parts[1];

        try {
            userTransaction.begin();

            Dispositivo dispositivo = daoService.create(new Dispositivo(uuid, daoService.find(Igreja.class, chaveIgreja)));

            Preferencias preferencias = new Preferencias(dispositivo);
            preferencias.setMinisteriosInteresse(daoService.findWith(QueryAcesso.MINISTERIOS_ATIVOS.create(chaveIgreja)));
            daoService.create(preferencias);

            userTransaction.commit();

            return dispositivo;
        } catch (Exception ex) {
            try {
                userTransaction.rollback();
            } catch (Exception ex0){}

            throw new RuntimeException(ex);
        }

    }

    @Asynchronous
    public void registraAcesso(String chaveDispositivo) {
        if (!DISPOSITIVOS_ACESSADOS.contains(chaveDispositivo)) {
            synchronized (DISPOSITIVOS_ACESSADOS) {
                DISPOSITIVOS_ACESSADOS.add(getDispositivo(chaveDispositivo).getChave());
            }
        }

    }

    public Dispositivo getDispositivo(String chaveDispositivo) {
        Dispositivo dispositivo = daoService.find(Dispositivo.class, chaveDispositivo);

        if (dispositivo == null) {
            synchronized (this) {
                dispositivo = daoService.find(Dispositivo.class, chaveDispositivo);

                if (dispositivo == null) {
                    dispositivo = createDispositivo(chaveDispositivo);
                }
            }
        }
        return dispositivo;
    }

    @Asynchronous
    public void registraLogin(String chaveDispositivo, Long idMembro, TipoDispositivo tipoDispositivo, String version) {
        Dispositivo dispositivo = getDispositivo(chaveDispositivo);

        try {
            userTransaction.begin();
            dispositivo.setMembro(daoService.find(Membro.class, idMembro));
            daoService.update(dispositivo);
            userTransaction.commit();
        } catch (Exception ex) {
            try {
                userTransaction.rollback();
            } catch (Exception ex0) {}

            LOGGER.log(Level.SEVERE, "Falha ao registrar o login do membro", ex);
        }

        registraPush(chaveDispositivo, tipoDispositivo, null, version);
    }

    @Asynchronous
    public void registraLogout(String chaveDispositivo) {
        Dispositivo dispositivo = getDispositivo(chaveDispositivo);

        try {
            userTransaction.begin();
            dispositivo.setMembro(null);
            daoService.update(dispositivo);
            userTransaction.commit();
        } catch (Exception ex) {
            try {
                userTransaction.rollback();
            } catch (Exception ex0) {}

            LOGGER.log(Level.SEVERE, "Falha ao registrar o logout do membro", ex);
        }
    }

    public enum TipoAcaoContigencia {
        FULL_RESET,
        FORCE_REGISTER,
        NENHUMA
    }

}
