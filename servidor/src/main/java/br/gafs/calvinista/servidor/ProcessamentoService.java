/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.dao.DAOService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.SystemException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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

    @PersistenceContext
    private EntityManager em;

    @EJB
    private DAOService daoService;

    @Resource
    private UserTransaction ut;

    @PostConstruct
    public void prepara() {
        for (int i=0;i<PROCESSMENTO_POOL_SIZE;i++){
            new Thread(new ProcessamentoRunnable()).start();
        }
        
        synchronized(this){
            try {
                for (Processamento processamento : Persister.load()){
                    pool.add(processamento);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            notifyAll();
        }
    }

    public void schedule(Processamento processamento){
        try {
            Persister.save(processamento);
            pool.add(processamento);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao tentar agendar processamento: " + processamento.getId(), e);
            Persister.remove(processamento.getId());
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

    public interface Processamento {
        String getId();
        int step(ProcessamentoTool tool) throws Exception;
        void finished(ProcessamentoTool tool) throws Exception;
        void dropped(ProcessamentoTool tool);
    }

    @Getter
    @RequiredArgsConstructor
    public class ProcessamentoTool {
        private final DAOService daoService;
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
            int total = 2;
            int fails = 0;

            
            ProcessamentoTool tool = new ProcessamentoTool(daoService);

            do{
                try {
                    LOGGER.log(Level.INFO, "Iniciando setp "+ tool.step +" de processamento: " + processamento.getId());
                    
                    ut.begin();
                    total = processamento.step(tool);
                    ut.commit();
                } catch (Exception e) {
                    try {
                        ut.rollback();
                        fails++;
                        if (fails >= LIMITE_FALHAS){
                            LOGGER.log(Level.SEVERE, "Processamento descartado por falhas: " + processamento.getId(), e);

                            ut.begin();
                            processamento.dropped(tool);
                            ut.commit();

                            Persister.remove(processamento.getId());
                            return;
                        }
                    } catch (Exception e1) {
                        LOGGER.log(Level.SEVERE, null, e1);
                    }
                }
            }while(tool.next(total));

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
                
                LOGGER.log(Level.SEVERE, "Erro ao finalizar processamento: " + processamento.getId(), e);
            }
                        
            Persister.remove(processamento.getId());
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
            pool.add(new Element(processamento,EMPTY_WATCHER));
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
        @EqualsAndHashCode(of = "processamento")
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

    public static final class Persister {
        private static final File dir = new File(ResourceBundleUtil._default().getPropriedade("PROCESSAMENTO_DIR"));
        private static final ObjectMapper om = new ObjectMapper();

        static {
            if (!dir.exists()){
                dir.mkdirs();
            }
        }

        public static void save(Processamento processamento) throws IOException {
            om.writeValue(new FileOutputStream((new File(dir, processamento.getId()))), new Storage(processamento));
        }

        public static void remove(String id){
            File processamento = new File(dir, id);
            if (processamento.exists()){
                processamento.delete();
            }
        }

        public static List<Processamento> load() throws IOException, ClassNotFoundException {
            Set<File> files = new TreeSet<File>(new Comparator<File>(){
                @Override
                public int compare(File o1, File o2) {
                    return (int) (o1.lastModified() - o2.lastModified());
                }
            });

            files.addAll(Arrays.asList(dir.listFiles()));

            List<Processamento> processamentos = new ArrayList<Processamento>();

            for (File file : files){
                Storage storage = om.readValue(file, Storage.class);
                processamentos.add((Processamento) storage.get());
            }

            return processamentos;
        }

        @Data
        @NoArgsConstructor
        public static class Storage {
            private String processamento;
            private String type;

            Storage(Processamento processamento) throws JsonProcessingException {
                this.processamento = om.writeValueAsString(processamento);
                this.type = processamento.getClass().getName();
            }

            <T> T get() throws ClassNotFoundException, IOException {
                return (T) om.readValue(processamento, Class.forName(type));
            }
        }
    }

}
