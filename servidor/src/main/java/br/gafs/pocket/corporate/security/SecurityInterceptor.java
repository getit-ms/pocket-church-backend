/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.security;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.StatusRegistroAcesso;
import br.gafs.pocket.corporate.servidor.EstatisticaService;
import br.gafs.pocket.corporate.servidor.SessaoBean;
import java.lang.reflect.Method;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 *
 * @author Gabriel
 */
public class SecurityInterceptor {
    
    @Inject
    private SessaoBean sessaoBean;

    @EJB
    private EstatisticaService estatisticaService;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception{
        if (!hasSeguranca(context.getMethod()) || 
                isColaboradorAutorizado(context.getMethod()) ||
                    isAdminAutorizado(context.getMethod()) ||
                        isUsuarioAutorizado(context.getMethod())){

            try {
                preparaRegistroAcesso(context);

                Object ret = context.proceed();

                registerAcesso(StatusRegistroAcesso.SUCESSO);

                return ret;
            } catch (Exception ex) {
                registerAcesso(StatusRegistroAcesso.FALHA);

                throw ex;
            }
        }else{
            throw new SecurityException();
        }
    }

    private void preparaRegistroAcesso(InvocationContext context) {
        if (context.getMethod().isAnnotationPresent(AcessoMarker.class)) {
            AcessoMarker marker = context.getMethod().getAnnotation(AcessoMarker.class);

            AcessoMarkerContext.funcionalidade(marker.value());
        }
    }

    private void registerAcesso(StatusRegistroAcesso status) {
        Funcionalidade funcionalidade = AcessoMarkerContext.funcionalidade();
        if (funcionalidade != null) {

            estatisticaService.registraAcesso(funcionalidade,
                    sessaoBean.getChaveDispositivo(), sessaoBean.getChaveEmpresa(), status);

            AcessoMarkerContext.clear();
        }
    }

    private boolean hasSeguranca(Method method){
        return method.isAnnotationPresent(AllowColaborador.class) ||
                method.isAnnotationPresent(AllowAdmin.class) ||
                method.isAnnotationPresent(AllowUsuario.class);
    }
    
    private boolean isUsuarioAutorizado(Method method) {
        if (method.isAnnotationPresent(AllowUsuario.class)){
            return sessaoBean.getIdUsuario() != null;
        }
        
        return false;
    }
    
    private boolean isColaboradorAutorizado(Method method) {
        if (method.isAnnotationPresent(AllowColaborador.class)){
            if (sessaoBean.getIdColaborador() != null){
                if (method.getAnnotation(AllowColaborador.class).value().length == 0) return true;
                for (Funcionalidade func : method.getAnnotation(AllowColaborador.class).value()){
                    if (sessaoBean.temPermissao(func)){
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private boolean isAdminAutorizado(Method method) {
        if (method.isAnnotationPresent(AllowAdmin.class)){
            if (sessaoBean.isAdmin()){
                if (method.getAnnotation(AllowAdmin.class).value().length == 0) return true;
                for (Funcionalidade func : method.getAnnotation(AllowAdmin.class).value()){
                    if (sessaoBean.temPermissao(func)){
                        return true;
                    }
                }
            }
            
        }
        
        return false;
    }
}