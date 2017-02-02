package br.gafs.calvinista.util;

import br.gafs.bundle.ResourceBundleUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mirante0 on 01/02/2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Persister {
    private static final File dir = new File(ResourceBundleUtil._default().getPropriedade("PERSISTER_DIR"));

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());

        MAPPER.setDateFormat(sdf);
        MAPPER.setTimeZone(TimeZone.getDefault());
    }

    public static File file(Class<?> type, Serializable id){
        return new File(new File(dir, type.getSimpleName()), id.toString());
    }

    public static void save(Object entity, String id) throws IOException {
        File file = file(entity.getClass(), id);

        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        MAPPER.writeValue(new FileOutputStream(file), new Storage(entity));
    }

    public static void remove(Class<?> type, String id){
        File processamento = file(type, id);
        if (processamento.exists()){
            processamento.delete();
        }
    }

    public static <T> T load(Class<T> type, String id) throws IOException, ClassNotFoundException {
        return (T) MAPPER.readValue(file(type, id), Storage.class).get();
    }

    public static <T> List<T> load(final Class<T> type) throws IOException, ClassNotFoundException {
        Set<File> files = new TreeSet<File>(new Comparator<File>(){
            @Override
            public int compare(File o1, File o2) {
                return (int) (o1.lastModified() - o2.lastModified());
            }
        });

        File typeDir = new File(dir, type.getSimpleName());

        if (typeDir.exists()){
            files.addAll(Arrays.asList(typeDir.listFiles()));
        }

        List<T> entities = new ArrayList<T>();

        for (File file : files){
            Storage storage = MAPPER.readValue(file, Storage.class);
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
            this.entity = MAPPER.writeValueAsString(entity);
            this.type = entity.getClass().getName();
        }

        <T> T get() throws ClassNotFoundException, IOException {
            return (T) MAPPER.readValue(entity, Class.forName(type));
        }
    }
}
