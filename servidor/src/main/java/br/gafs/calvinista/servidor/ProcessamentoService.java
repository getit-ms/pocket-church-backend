/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.domain.StatusBoletim;
import br.gafs.util.email.EmailUtil;
import br.gafs.util.exception.ExceptionUnwrapperUtil;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

/**
 *
 * @author Gabriel
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ProcessamentoService {
    private static final int LIMITE_TENTATIVAS = 5;
    private static final int FILA_TRANSACAO = 5;
    
    @PersistenceContext
    private EntityManager em;
    
    @Resource
    private UserTransaction ut;
    
    @Asynchronous
    public void processa(Processo processo) throws Exception {
        int falhas = 0;
        
        processo.begin();
        
        while (true){
            try{
                
                boolean complete;
                do {
                    ut.begin();
                    
                    complete = processo.execute(FILA_TRANSACAO);
                    
                    ut.commit();
                }while(!complete);
                
                ut.begin();
                
                processo.success();
                
                ut.commit();
                
                break;
            }catch(Exception e){
                ut.rollback();
                
                if (falhas >= LIMITE_TENTATIVAS){
                    Throwable t = ExceptionUnwrapperUtil.unwrappException(e);
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    EmailUtil.alertAdm(sw.toString(), "Erro em processamento: " + t.getMessage());
                    
                    ut.begin();
                    
                    processo.fail();
                    
                    ut.commit();
                    
                    break;
                }
                
                falhas++;
            }
        }
    }
    
    public interface Processo extends Serializable {
        public void begin();
        public boolean execute(int count) throws Exception;
        public void success();
        public void fail();
    }
    
}
