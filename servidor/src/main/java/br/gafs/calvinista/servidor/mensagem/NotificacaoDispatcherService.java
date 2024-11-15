package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.dao.FiltroDispositivoNotificacao;
import br.gafs.calvinista.dao.QueryNotificacao;
import br.gafs.calvinista.dao.RegisterSentNotifications;
import br.gafs.calvinista.dto.FiltroDispositivoNotificacaoDTO;
import br.gafs.calvinista.dto.MensagemPushDTO;
import br.gafs.calvinista.entity.domain.TipoDispositivo;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 17/01/2018.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class NotificacaoDispatcherService {

    private static final Logger LOGGER = Logger.getLogger(NotificacaoDispatcherService.class.getName());

    private static final int TIPO = 0;
    private static final int INSTALATION_ID = 1;
    private static final int CHAVE_DISPOSITIVO = 2;
    private static final int ID_MEMBRO = 3;
    private static final int VERSAO = 4;

    @EJB
    private CustomDAOService daoService;

    @PersistenceContext
    private EntityManager em;

    @EJB
    private AndroidNotificationService androidService;

    @EJB
    private FirebaseService firebaseService;

    @EJB
    private IPBNotificationService ipbService;

    @EJB
    private IOSNotificationService iosService;

    @Resource
    private UserTransaction transaction;

    @Asynchronous
    public void enviaNotificacoes(Long idNotificacao, FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push) {
        LOGGER.log(Level.SEVERE, "enviaNotificacoes inicido para: " + idNotificacao);

        LOGGER.info("Buscando registro para envio do push: " + idNotificacao + " página " + filtro.getPagina());

        FiltroDispositivoNotificacao fquery = new FiltroDispositivoNotificacao(filtro);

        List<Object[]> pagina = em.createNativeQuery(fquery.getQuery())
                .setFirstResult(fquery.getResultLimit() * (fquery.getPage() - 1))
                .setMaxResults(fquery.getResultLimit())
                .getResultList();

        if (pagina.isEmpty()) {
            LOGGER.info("Nenhum registro encontrado para envio de push.");
        } else {
            try {
                LOGGER.info("Enviando push: " + idNotificacao + " para " + pagina.size() + " dispositivos.");

                enviaNotificacoesPagina(idNotificacao, filtro, push, pagina);
            } catch (Exception e) {

                LOGGER.warning("Erro ao enviar página de notificações: " + idNotificacao);

                for (Object[] dispositivo : pagina) {
                    try {
                        enviaNotificacao(idNotificacao, filtro, push, dispositivo);
                    } catch (Exception ex1) {
                        LOGGER.log(Level.SEVERE, "Erro ao enviar push " +
                                idNotificacao + " para " + dispositivo[CHAVE_DISPOSITIVO], ex1);
                    }
                }

            }
        }
    }

    private void enviaNotificacoesPagina(Long idNotificacao, FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push, List<Object[]> dispositivos)
            throws SystemException, HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException {

        List<IOSNotificationService.Destination> destinationIOS = new ArrayList<>();
        List<AndroidNotificationService.Destination> destinationAndroid = new ArrayList<>();
        List<IPBNotificationService.Destination> destinationIPB = new ArrayList<>();
        List<FirebaseService.Destination> destinationFirebase = new ArrayList<>();

        List<String> ids = new ArrayList<>();
        for (Object[] dispositivo : dispositivos) {
            long count = countNotificacoesNaoLidas(filtro.getIgreja().getChave(), (String) dispositivo[CHAVE_DISPOSITIVO], (Long) dispositivo[ID_MEMBRO]) + 1;

            if (dispositivo[TIPO].equals(TipoDispositivo.ANDROID.ordinal())) {
                destinationAndroid.add(new AndroidNotificationService.Destination((String) dispositivo[INSTALATION_ID], count));
            } else if (dispositivo[TIPO].equals(TipoDispositivo.IPHONE.ordinal())) {
                destinationIOS.add(new IOSNotificationService.Destination((String) dispositivo[INSTALATION_ID], count));
            } else if (dispositivo[TIPO].equals(TipoDispositivo.IPB.ordinal())) {
                destinationIPB.add(new IPBNotificationService.Destination((String) dispositivo[INSTALATION_ID], count));
            } else if (dispositivo[TIPO].equals(TipoDispositivo.ANDROID_FIREBASE.ordinal())) {
                destinationFirebase.add(new FirebaseService.Destination((String) dispositivo[INSTALATION_ID], (String) dispositivo[VERSAO], count));
            } else if (dispositivo[TIPO].equals(TipoDispositivo.IPHONE_FIREBASE.ordinal())) {
                destinationFirebase.add(new FirebaseService.Destination((String) dispositivo[INSTALATION_ID], (String) dispositivo[VERSAO], count));
            }

            ids.add((String) dispositivo[INSTALATION_ID]);
        }

        if (!ids.isEmpty()) {
            try {
                transaction.begin();

                daoService.execute(new RegisterSentNotifications(idNotificacao, ids));

                transaction.commit();
            } catch (Exception ex) {
                transaction.rollback();
                throw ex;
            }
        }

        if (!destinationAndroid.isEmpty()) {
            androidService.pushNotifications(filtro.getIgreja(), push, destinationAndroid);
        }

        if (!destinationIOS.isEmpty()) {
            iosService.pushNotifications(filtro.getIgreja(), push, destinationIOS);
        }

        if (!destinationIPB.isEmpty()) {
            ipbService.pushNotifications(filtro.getIgreja(), push, destinationIPB);
        }

        if (!destinationFirebase.isEmpty()) {
            firebaseService.pushNotifications(filtro.getIgreja(), push, destinationFirebase);
        }
    }

    private long countNotificacoesNaoLidas(String igreja, String dispositivo, Long membro) {
        if (membro != null) {
            return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS_MEMBRO.createSingle(igreja, membro));
        } else {
            return daoService.findWith(QueryNotificacao.COUNT_NOTIFICACOES_NAO_LIDAS_DISPOSITIVO.createSingle(igreja, dispositivo));
        }
    }

    private void enviaNotificacao(Long idNotificacao, FiltroDispositivoNotificacaoDTO filtro, MensagemPushDTO push, Object[] dispositivo)
            throws SystemException, HeuristicRollbackException, RollbackException, NotSupportedException, HeuristicMixedException {
        List<Object[]> dispositivos = new ArrayList<>();
        dispositivos.add(dispositivo);
        enviaNotificacoesPagina(idNotificacao, filtro, push, dispositivos);
    }
}
