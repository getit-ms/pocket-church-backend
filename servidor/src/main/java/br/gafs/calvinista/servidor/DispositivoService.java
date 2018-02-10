package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.dao.DAOService;
import br.gafs.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 07/02/2018.
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class DispositivoService {

    public static final Logger LOGGER = Logger.getLogger(DispositivoService.class.getName());

    public final List<RegisterPushDTO> REGISTER_DEVICES = new ArrayList<RegisterPushDTO>();

    private static final File CONTINGENCIA_DIR = new File("/calvin/contingencia");
    private static final String FORCE_REGISTER_MASK = "force_register_{0}";
    private static final String FULL_RESET_MASK = "full_reset_{0}";

    @EJB
    private DAOService daoService;

    @Resource
    private UserTransaction userTransaction;

    @Asynchronous
    public void register(String chaveDispositivo, TipoDispositivo tipoDispositivo, String pushKey, String version) {
        LOGGER.info("Chamada para registro de dispositivo " + tipoDispositivo + " - " + pushKey + " - " + version);

        synchronized(REGISTER_DEVICES) {
            REGISTER_DEVICES.add(new RegisterPushDTO(chaveDispositivo, tipoDispositivo, pushKey, version));
        }
    }

    @Schedule(hour = "*", minute = "*")
    public void doRegisterPush(){
        LOGGER.info("Iniciando flush de registros de push");

        List<RegisterPushDTO> register = new ArrayList<RegisterPushDTO>();
        synchronized (REGISTER_DEVICES){
            register.addAll(REGISTER_DEVICES);
        }

        if (register.isEmpty()) {
            LOGGER.info("Nenhum dispositivo a ser registrado.");
            return;
        }

        while (register.size() > 30) {
            register.remove(register.size() - 1);
        }

        LOGGER.info("Processando " + register.size() + " registros de push.");
        try {
            
            registraEmGrupo(register);

        }catch (Exception ex) {
            try {
                userTransaction.rollback();
            } catch (SystemException e) {}

            LOGGER.log(Level.SEVERE, "Erro ao persistir grupo de dispositivos. Tentando persistência individual.", ex);

            registraIndividualmente(register);
        }

        synchronized (REGISTER_DEVICES){
            REGISTER_DEVICES.removeAll(register);

            LOGGER.info("Registros de push concluídos. " + REGISTER_DEVICES.size() + " restantes.");
        }
    }

    private void registraEmGrupo(List<RegisterPushDTO> register) throws NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        Iterator<RegisterPushDTO> iterator = register.iterator();

        userTransaction.begin();

        int size = 1;
        while (iterator.hasNext()){
            RegisterPushDTO dto = iterator.next();

            LOGGER.info("Registro push em grupo para " + dto.getDispositivo());

            trataDispositivo(iterator, dto);
            
            size++;
            
            if (size%20 == 0) {
                userTransaction.commit();
                
                userTransaction.begin();
            }
        }

        if (size%20 != 0) {
            userTransaction.commit();
        }
    }

    private void registraIndividualmente(List<RegisterPushDTO> register) {
        Iterator<RegisterPushDTO> iterator = register.iterator();

        while (iterator.hasNext()){
            RegisterPushDTO dto = iterator.next();
            try {
                userTransaction.begin();

                LOGGER.info("Registro push individual para " + dto.getDispositivo());

                trataDispositivo(iterator, dto);

                userTransaction.commit();
            }catch(Exception ex0) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e) {}

                LOGGER.log(Level.SEVERE, "Dispositivo " + dto.getDispositivo() + " não pode ser registrado. Será removido da lista para não impedir novos registros", ex0);
            }
        }
    }

    private void trataDispositivo(Iterator<RegisterPushDTO> iterator, RegisterPushDTO dto) {
        Dispositivo dispositivo = daoService.find(Dispositivo.class, dto.getDispositivo());

        if (dispositivo != null){
            LOGGER.info("Dispositivo " + dto.getDispositivo() + " existe. Registrando push " + dto.getTipo() + "  - " + dto.getPushkey() + " = " + dto.getVersion());

            dispositivo.registerToken(dto.getTipo(), dto.getPushkey(), dto.getVersion());

            dispositivo = daoService.update(dispositivo);

            daoService.execute(QueryAcesso.UNREGISTER_OLD_DEVICES.create(dispositivo.getPushkey(), dispositivo.getChave()));
        }else{
            LOGGER.info("Dispositivo não existe. Adiando registro.");

            iterator.remove();
        }
    }

    public TipoAcaoContigencia verificaContingencia(String chaveDispositivo) {
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

            return TipoAcaoContigencia.FULL_RESET;
        }

        return TipoAcaoContigencia.NENHUMA;
    }

    public enum TipoAcaoContigencia {
        FULL_RESET,
        FORCE_REGISTER,
        NENHUMA
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode(of = "dispositivo")
    static class RegisterPushDTO implements DTO {
        private String dispositivo;
        private TipoDispositivo tipo;
        private String pushkey;
        private String version;
    }
}
