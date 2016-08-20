/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.security;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.servidor.SessaoBean;
import java.lang.reflect.Method;
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
    
    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception{
        if (!hasSeguranca(context.getMethod()) || 
                isMembroAutorizado(context.getMethod()) || 
                    isAdminAutorizado(context.getMethod()) ||
                        isUsuarioAutorizado(context.getMethod())){
            return context.proceed();
        }else{
            throw new SecurityException();
        }
    }
    
    private boolean hasSeguranca(Method method){
        return method.isAnnotationPresent(AllowMembro.class) ||
                method.isAnnotationPresent(AllowAdmin.class) ||
                method.isAnnotationPresent(AllowUsuario.class);
    }
    
    private boolean isUsuarioAutorizado(Method method) {
        if (method.isAnnotationPresent(AllowUsuario.class)){
            return sessaoBean.getIdUsuario() != null;
        }
        
        return false;
    }
    
    private boolean isMembroAutorizado(Method method) {
        if (method.isAnnotationPresent(AllowMembro.class)){
            if (sessaoBean.getIdMembro() != null){
                if (method.getAnnotation(AllowMembro.class).value().length == 0) return true;
                for (Funcionalidade func : method.getAnnotation(AllowMembro.class).value()){
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
