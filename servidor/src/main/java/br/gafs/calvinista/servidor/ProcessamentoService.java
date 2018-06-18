/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.bean.IEntity;
import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.util.Persister;
import br.gafs.dao.DAOService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Gabriel
 */
@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class ProcessamentoService {
    private static final Logger LOGGER = Logger.getLogger(ProcessamentoService.class.getName());
    private static final int PROCESSMENTO_POOL_SIZE = ResourceBundleUtil._default().getPropriedadeAsInteger("PROCESSMENTO_POOL_SIZE");

    private final Pool pool = new Pool();

    @EJB
    private DAOService daoService;

    @Resource
    private SessionContext sctx;

    @Resource
    private UserTransaction ut;

    @PostConstruct
    public void prepara() {
        for (int i=0;i<PROCESSMENTO_POOL_SIZE;i++){
            new Thread(new ProcessamentoRunnable()).start();
        }

        synchronized(this){
            try {
                for (Processamento processamento : Persister.load(Processamento.class)){
                    pool.add(processamento);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            notifyAll();
        }
    }

    @Asynchronous
    public void schedule(Processamento processamento){
        try {
            Persister.save(processamento, processamento.getId());
            pool.add(processamento);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao tentar agendar entity: " + processamento.getId(), e);
            Persister.remove(Processamento.class, processamento.getId());
        }
    }

    public void execute(final Processamento processamento) throws InterruptedException {
        synchronized (processamento){
            pool.priority(processamento, new Pool.Watcher() {
                @Override
                public void started() {

                }

                @Override
                public void ended() {
                    synchronized(processamento){
                        processamento.notify();
                    }
                }
            });

            processamento.wait();
        }
    }

    public interface Processamento extends IEntity {
        String getId();
        int step(ProcessamentoTool tool) throws Exception;
        void finished(ProcessamentoTool tool) throws Exception;
        void dropped(ProcessamentoTool tool);
    }

    @Getter
    @RequiredArgsConstructor
    public class ProcessamentoTool {
        private final DAOService daoService;
        private final SessionContext sessionContext;
        private int step = 1;

        protected boolean next(int total){
            return ++step <= total;
        }
    }

    private static final int LIMITE_FALHAS = ResourceBundleUtil._default().getPropriedadeAsInteger("LIMITE_FALHAS_PROCESSAMENTO");

    final class ProcessamentoRunnable implements Runnable, Pool.Executor {

        @Override
        public void run() {
            try {
                while (true){
                    pool.next(this);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void execute(Processamento processamento) {
            int total = 1;
            int fails = 0;

            ProcessamentoTool tool = new ProcessamentoTool(daoService, sctx);

            boolean fail;
            do{
                fail = false;
                try {
                    LOGGER.log(Level.INFO, "Iniciando step "+ tool.step +" do processamento: " + processamento.getClass() + " - " + processamento.getId());

                    ut.begin();
                    total = processamento.step(tool);
                    ut.commit();
                } catch (Exception e) {
                    fail = true;

                    try {
                        ut.rollback();
                    } catch (Exception e1) {}

                    try {
                        fails++;
                        if (fails >= LIMITE_FALHAS){
                            LOGGER.log(Level.SEVERE, "Processamento descartado por falhas: " + processamento.getClass() + " - " + processamento.getId(), e);

                            ut.begin();
                            processamento.dropped(tool);
                            ut.commit();

                            Persister.remove(processamento.getClass(), processamento.getId());
                            return;
                        }else{
                            LOGGER.log(Level.SEVERE, "Processamento com erro: " + processamento.getClass() + " - " + processamento.getId(), e);
                            continue;
                        }
                    } catch (Exception e1) {
                        LOGGER.log(Level.SEVERE, null, e1);
                        continue;
                    }
                }
            }while(fail || tool.next(total));

            try{
                ut.begin();
                processamento.finished(tool);
                ut.commit();
            }catch(Exception e){
                try {
                    ut.rollback();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }

                LOGGER.log(Level.SEVERE, "Erro ao finalizar entity: " + processamento.getId(), e);
            }

            Persister.remove(processamento.getClass(), processamento.getId());
        }
    }

    static final class Pool {
        private static final Watcher EMPTY_WATCHER = new Watcher() {
            @Override
            public void started() {}

            @Override
            public void ended() {}
        };

        private final List<Element> pool = new ArrayList<Element>();

        public synchronized void add(Processamento processamento){
            Element element = new Element(processamento,EMPTY_WATCHER);

            pool.remove(element);
            pool.add(element);
            notify();
        }

        public synchronized void priority(Processamento processamento, Watcher watcher) throws InterruptedException {
            Element element = new Element(processamento, watcher);

            pool.remove(element);
            pool.add(0, element);
            notify();
        }

        public synchronized void next(Executor executor) throws InterruptedException {
            while (pool.isEmpty()){
                wait();
            }

            Element element = pool.remove(0);
            element.getWatcher().started();
            executor.execute(element.getProcessamento());
            element.getWatcher().ended();
        }

        @Getter
        @AllArgsConstructor
        @EqualsAndHashCode(of = "entity")
        static class Element {
            private Processamento processamento;
            private Watcher watcher;
        }

        interface Watcher {
            void started();
            void ended();
        }

        interface Executor {
            void execute(Processamento processamento);
        }
    }

}
