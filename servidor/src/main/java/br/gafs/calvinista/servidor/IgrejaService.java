package br.gafs.calvinista.servidor;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.StatusIgreja;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.util.*;

@Singleton
public class IgrejaService {
    private final Map<String, StatusIgreja> status = new HashMap<>();

    @EJB
    private DAOService daoService;

    @Schedule(hour = "*")
    public void refreshStatus() {
        List<String> strings = new ArrayList<>(status.keySet());
        for (String chave : strings) {
            refreshStatus(chave);
        }
    }

    private boolean refreshStatus(String chave) {
        Igreja igreja = daoService.find(Igreja.class, chave);

        synchronized (status) {
            if (igreja == null) {
                status.remove(chave);
                return false;
            } else {
                status.put(chave, igreja.getStatus());
                return true;
            }
        }
    }

    @Schedule(hour = "0")
    public void clearStatus() {
        synchronized (status) {
            status.clear();
        }
    }

    public void checkStatusIgreja(String chave, boolean admin) {
        StatusIgreja status = getStatus(chave);

        if (StatusIgreja.INATIVO.equals(status)) {
            throw new ServiceException("O aplicativo foi desativado. Entre em contato com a igreja para mais detalhes");
        }

        if (StatusIgreja.BLOQUEADO.equals(status) && admin) {
            throw new ServiceException("O aplicativo está bloqueado. Entre em contato com a GET IT para mais detalhes.");
        }
    }

    private StatusIgreja getStatus(String chave) {
        if (!status.containsKey(chave)) {
            synchronized (status) {
                if (!status.containsKey(chave) && !refreshStatus(chave)) {
                    throw new ServiceException("mensagens.MSG-403");
                }
            }
        }

        return status.get(chave);
    }
}
