/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.security;

import br.gafs.calvinista.entity.RegistroAuditoria;
import br.gafs.calvinista.entity.domain.StatusRegistroAuditoria;
import br.gafs.calvinista.service.AuditoriaService;
import br.gafs.calvinista.servidor.SessaoBean;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.util.exception.ExceptionUnwrapperUtil;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * @author Gabriel
 */
public class AuditoriaInterceptor {

    @Inject
    private SessaoBean sessaoBean;

    @EJB
    private AuditoriaService auditoriaService;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        RegistroAuditoria auditoria = new RegistroAuditoria(
                sessaoBean.getChaveDispositivo(),
                sessaoBean.getIdMembro(),
                sessaoBean.getChaveIgreja(),
                context.getMethod().getDeclaringClass().getName() + "." +
                        context.getMethod().getName() +
                        Arrays.asList(context.getMethod().getParameterTypes()));

        try {

            Object retorno = context.proceed();
            if (context.getMethod().isAnnotationPresent(Audit.class)) {
                auditoriaService.registra(auditoria, context.getParameters(), retorno, StatusRegistroAuditoria.SUCESSO);
            }
            return retorno;

        } catch (Exception e) {
            try {
                throw ExceptionUnwrapperUtil.unwrappException(e);
            } catch (ServiceException | ServiceExceptionList ex) {
                if (context.getMethod().isAnnotationPresent(Audit.class)) {
                    auditoriaService.registra(auditoria, context.getParameters(), toString(ex), StatusRegistroAuditoria.ERRO_VALIDACAO);
                }
            } catch (Throwable ex) {
                auditoriaService.registra(auditoria, context.getParameters(), toString(ex), StatusRegistroAuditoria.ERRO_SISTEMA);
            }

            throw e;
        }
    }

    private static String toString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
