/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.entity.RegistroAuditoria;
import br.gafs.calvinista.entity.domain.StatusRegistroAuditoria;
import br.gafs.calvinista.security.AuditoriaInterceptor;
import br.gafs.calvinista.service.AuditoriaService;
import br.gafs.logger.ServiceLoggerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gabriel
 */
@Stateless
@Local(AuditoriaService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class})
public class AuditoriaServiceImpl implements AuditoriaService {

    @EJB
    private CustomDAOService daoService;

    private final ObjectMapper om = new ObjectMapper();

    private final static List<RegistroAuditoria> lote = new ArrayList<RegistroAuditoria>();

    @Override
    @Asynchronous
    public void registra(RegistroAuditoria auditoria, Object request, Object response, StatusRegistroAuditoria status) {
        auditoria.setStatus(status);

        if (request != null) {
            try {
                auditoria.setRequest(om.writeValueAsString(request));
            } catch (IOException ex) {
                Logger.getLogger(AuditoriaServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (response != null) {
            try {
                auditoria.setResponse(om.writeValueAsString(response));
            } catch (IOException ex) {
                Logger.getLogger(AuditoriaServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        synchronized (lote) {
            lote.add(auditoria);
        }
    }

    @Schedule(hour = "*", minute = "*", persistent = false)
    public void flushAuditoria() {
        List<RegistroAuditoria> audit = new ArrayList<RegistroAuditoria>();

        synchronized (lote) {
            audit.addAll(lote);
            lote.clear();
        }

        for (RegistroAuditoria auditoria : audit) {
            daoService.create(auditoria);
        }
    }
}
