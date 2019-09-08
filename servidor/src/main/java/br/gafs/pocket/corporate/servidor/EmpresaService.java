package br.gafs.pocket.corporate.servidor;

import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.domain.StatusEmpresa;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.*;

@Startup
@Singleton
public class EmpresaService {
    private final Map<String, StatusEmpresa> status = new HashMap<>();

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
        Empresa empresa = daoService.find(Empresa.class, chave);

        synchronized (status) {
            if (empresa == null) {
                status.remove(chave);
                return false;
            } else {
                status.put(chave, empresa.getStatus());
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

    public void checkStatusEmpresa(String chave, boolean admin) {
        StatusEmpresa status = getStatus(chave);

        if (StatusEmpresa.INATIVO.equals(status)) {
            throw new ServiceException("O aplicativo foi desativado. Entre em contato com a empresa para mais detalhes");
        }

        if (StatusEmpresa.BLOQUEADO.equals(status) && admin) {
            throw new ServiceException("O aplicativo está bloqueado. Entre em contato com a GET IT para mais detalhes.");
        }
    }

    private StatusEmpresa getStatus(String chave) {
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
