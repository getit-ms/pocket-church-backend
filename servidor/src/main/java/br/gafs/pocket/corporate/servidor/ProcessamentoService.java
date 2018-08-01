/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.servidor;

import br.gafs.bean.IEntity;
import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.pocket.corporate.util.Persister;
import br.gafs.dao.DAOService;
import lombok.AllArgsConstructor;
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
            pool.priority(processamento, new ProcessamentoWatcher() {
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
        private final SessionContext sessionContext;
        private int step = 1;

        public <T> T transactional(ExecucaoTransacional<T> execucao) {
            synchronized (ut) {
                try {
                    LOGGER.info("begin de transação");

                    ut.begin();
                    T t = execucao.execute(daoService);

                    LOGGER.info("commit de transação");
                    ut.commit();
                    return t;
                } catch (Exception ex) {
                    LOGGER.info("rollback de transação");

                    try {
                        ut.rollback();
                    } catch(Exception ex0){}

                    throw new RuntimeException(ex);
                }
            }
        }

        protected boolean next(int total){
            return ++step <= total;
        }
    }

    public interface ExecucaoTransacional<T> {
        T execute(DAOService daoService);
    }

    private static final int LIMITE_FALHAS = ResourceBundleUtil._default().getPropriedadeAsInteger("LIMITE_FALHAS_PROCESSAMENTO");

    final class ProcessamentoRunnable implements Runnable {

        @Override
        public void run() {
            try {
                while (true){
                    Pool.Element element = pool.next();

                    element.getWatcher().started();
                    execute(element.getProcessamento());
                    element.getWatcher().ended();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void execute(Processamento processamento) {
            int total = 1;
            int fails = 0;

            ProcessamentoTool tool = new ProcessamentoTool(sctx);

            boolean fail;
            do{
                fail = false;
                try {
                    LOGGER.log(Level.INFO, "Iniciando step "+ tool.step +" do processamento: " + processamento.getClass() + " - " + processamento.getId());

                    total = processamento.step(tool);
                } catch (Exception e) {
                    fail = true;

                    try {
                        fails++;
                        if (fails >= LIMITE_FALHAS){
                            LOGGER.log(Level.SEVERE, "Processamento descartado por falhas: " + processamento.getClass() + " - " + processamento.getId(), e);

                            processamento.dropped(tool);

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
                processamento.finished(tool);
            }catch(Exception e){
                LOGGER.log(Level.SEVERE, "Erro ao finalizar entity: " + processamento.getId(), e);
            }

            Persister.remove(processamento.getClass(), processamento.getId());
        }

    }

    interface ProcessamentoWatcher {
        void started();
        void ended();
    }

    static final class Pool {
        private static final List<Processamento> running = new ArrayList<>();

        private static final ProcessamentoWatcher EMPTY_WATCHER = new ProcessamentoWatcher() {
            @Override
            public void started() {}

            @Override
            public void ended() {}
        };

        private final List<Element> pool = new ArrayList<Element>();

        public synchronized void add(Processamento processamento){
            Element element = new Element(processamento, new WatcherPool(processamento, EMPTY_WATCHER));

            synchronized (running) {
                if (!pool.contains(element) &&
                        !running.contains(element.getProcessamento())) {
                    pool.add(element);
                }
            }

            notify();
        }

        public synchronized void priority(Processamento processamento, ProcessamentoWatcher watcher) throws InterruptedException {
            Element element = new Element(processamento, new WatcherPool(processamento, watcher));

            pool.remove(element);
            pool.add(0, element);
            notify();
        }

        public synchronized Element next() throws InterruptedException {
            while (pool.isEmpty()){
                wait();
            }

            return pool.remove(0);
        }

        @Getter
        @AllArgsConstructor
        static class Element {
            private Processamento processamento;
            private ProcessamentoWatcher watcher;

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Element) {
                    Element other = (Element) obj;
                    return getProcessamento().equals(other.getProcessamento());
                }

                return false;
            }

        }

        @RequiredArgsConstructor
        private class WatcherPool implements ProcessamentoWatcher {
            private final Processamento processamento;
            private final ProcessamentoWatcher delegate;

            @Override
            public void started() {
                synchronized (running) {
                    running.add(processamento);
                }
                delegate.started();
            }

            @Override
            public void ended() {
                synchronized (running) {
                    running.remove(processamento);
                }
                delegate.ended();
            }
        }
    }

}
