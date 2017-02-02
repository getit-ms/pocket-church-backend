package br.gafs.calvinista.util;

import br.gafs.bean.IEntity;
import br.gafs.bundle.ResourceBundleUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.*;

/**
 * Created by mirante0 on 01/02/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Persister {
    private static final File dir = new File(ResourceBundleUtil._default().getPropriedade("PERSISTER_DIR"));
    private static final ObjectMapper om = new ObjectMapper();

    public static File file(Class<?> type, Serializable id){
        return new File(new File(dir, type.getName().replaceAll("[\\.\\:]", "_")), id.toString());
    }

    public static void save(Object entity, String id) throws IOException {
        File file = file(entity.getClass(), id);
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        om.writeValue(new FileOutputStream(file), new Storage(entity));
    }

    public static void remove(Class<?> type, String id){
        File processamento = file(type, id);
        if (processamento.exists()){
            processamento.delete();
        }
    }

    public static <T> T load(Class<T> type, String id) throws IOException, ClassNotFoundException {
        return (T) om.readValue(file(type, id), Storage.class).get();
    }

    public static <T> List<T> load(final Class<T> type) throws IOException, ClassNotFoundException {
        Set<File> files = new TreeSet<File>(new Comparator<File>(){
            @Override
            public int compare(File o1, File o2) {
                return (int) (o1.lastModified() - o2.lastModified());
            }
        });

        files.addAll(Arrays.asList(dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(type.getName() + "#");
            }
        })));

        List<T> entities = new ArrayList<T>();

        for (File file : files){
            Storage storage = om.readValue(file, Storage.class);
            entities.add((T) storage.get());
        }

        return entities;
    }

    @Data
    @NoArgsConstructor
    public static class Storage {
        private String entity;
        private String type;

        Storage(Object entity) throws JsonProcessingException {
            this.entity = om.writeValueAsString(entity);
            this.type = entity.getClass().getName();
        }

        <T> T get() throws ClassNotFoundException, IOException {
            return (T) om.readValue(entity, Class.forName(type));
        }
    }
}
