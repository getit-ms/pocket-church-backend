/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.entity.domain;

import br.gafs.calvinista.entity.Parametro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@RequiredArgsConstructor
public enum TipoParametro {
    REPOSITORY_URL(TipoValor.VALOR, String.class, null),
    GERACAO_POOL_SIZE(TipoValor.VALOR, Integer.class, "5"),

    USER_PAGSEGURO(TipoValor.VALOR, String.class, ""),
    TOKEN_PAGSEGURO(TipoValor.VALOR, String.class, ""),
    HABILITADO_PAGSEGURO(TipoValor.VALOR, boolean.class, "false"),
    TITULO_VERSICULO_DIARIO(TipoValor.VALOR, String.class, null),
    TITULO_LEMBRETE_LEITURA_BIBLICA(TipoValor.VALOR, String.class, null),
    TITULO_ANIVERSARIO(TipoValor.VALOR, String.class, null),
    TEXTO_ANIVERSARIO(TipoValor.VALOR, String.class, null),
    TITULO_BOLETIM(TipoValor.VALOR, String.class, null),
    TEXTO_BOLETIM(TipoValor.VALOR, String.class, null),
    TITULO_ESTUDO(TipoValor.VALOR, String.class, null),
    TEXTO_ESTUDO(TipoValor.VALOR, String.class, null),
    TITULO_YOUTUBE_AO_VIVO(TipoValor.VALOR, String.class, null),
    TEXTO_YOUTUBE_AO_VIVO(TipoValor.VALOR, String.class, null),
    TITULO_YOUTUBE_AGENDADO(TipoValor.VALOR, String.class, null),
    TEXTO_YOUTUBE_AGENDADO(TipoValor.VALOR, String.class, null),

    PUSH_ANDROID_KEY(TipoValor.VALOR, String.class, null),
    PUSH_ANDROID_SENDER_ID(TipoValor.VALOR, String.class, null),
    PUSH_IOS_PASS(TipoValor.VALOR, String.class, null),
    PUSH_IOS_CERTIFICADO(TipoValor.ANEXO, byte[].class, null),

    GOOGLE_OAUTH_CLIENT_KEY(TipoValor.VALOR, String.class, null),
    GOOGLE_OAUTH_SECRET_KEY(TipoValor.VALOR, String.class, null),
    YOUTUBE_CHANNEL_ID(TipoValor.VALOR, String.class, null),
            
    ;
    
    private final TipoValor tipoValor;
    private final Class<?> runtimeType;
    private final Object defaultValue;
    
    private static final Map<Class<?>, Map<Class<?>, Converter<?,?>>> converters = new HashMap();
    
    static {
        converters.put(String.class, new HashMap());
        
        converters.get(String.class).put(Integer.class, new Converter<String, Integer>(){
            
            @Override
            public Integer sourceToTarget(String source) {
                return Integer.parseInt(source);
            }
            
            @Override
            public String targetToSource(Integer target) {
                return target.toString();
            }
            
        });
        converters.get(String.class).put(boolean.class, new Converter<String, Boolean>(){
            
            @Override
            public Boolean sourceToTarget(String source) {
                return Boolean.parseBoolean(source);
            }
            
            @Override
            public String targetToSource(Boolean target) {
                return target.toString();
            }
            
        });
        converters.get(String.class).put(Boolean.class, new Converter<String, Boolean>(){
            
            @Override
            public Boolean sourceToTarget(String source) {
                return Boolean.parseBoolean(source);
            }
            
            @Override
            public String targetToSource(Boolean target) {
                return target.toString();
            }
            
        });
        
        converters.put(byte[].class, new HashMap());
        
        converters.get(byte[].class).put(byte[].class, new Converter<byte[], byte[]>(){

            @Override
            public byte[] sourceToTarget(byte[] source) {
                return source;
            }

            @Override
            public byte[] targetToSource(byte[] target) {
                return target;
            }
            
        });
    }
    
    public <T> T get(Parametro param) {
        return (T) converter().sourceToTarget(
                tipoValor.get(param) != null ?
                        tipoValor.get(param) : defaultValue
        );
    }
    
    public <T> void set(Parametro param, T valor) {
        tipoValor.set(param, valor == null ? defaultValue :
                converter().targetToSource(valor));
    }
    
    private Converter converter(){
        Converter converter = null;
        if (converters.containsKey(tipoValor.getDbType()) &&
                converters.get(tipoValor.getDbType()).containsKey(runtimeType)){
            converter = converters.get(tipoValor.getDbType()).get(runtimeType);
        }
        
        return converter == null ? tipoValor.defaultConverter() : converter;
    }
    
    interface Converter<SRC, TGT> {
        TGT sourceToTarget(SRC source);
        SRC targetToSource(TGT target);
    }
    
    public static <T> T build(Class<T> type, ParametroSupplier supplier){
        try {
            T t = type.newInstance();
            
            for (Field field : type.getDeclaredFields()){
                try {
                    if (field.isAnnotationPresent(Mapping.class)){
                        boolean accessible = field.isAccessible();
                        field.setAccessible(true);
                        field.set(t, supplier.get(field.getAnnotation(Mapping.class).value()).get());
                        field.setAccessible(accessible);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(TipoParametro.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            return t;
        } catch (Exception ex) {
            Logger.getLogger(TipoParametro.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static <T> T extract(Object t, ParametroSupplier supplier, ParametroHandler handler){
        for (Field field : t.getClass().getDeclaredFields()){
            try {
                if (field.isAnnotationPresent(Mapping.class)){
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    Parametro param = supplier.get(field.getAnnotation(Mapping.class).value());
                    param.set(field.get(t));
                    field.setAccessible(accessible);
                    handler.handle(param);
                }
            } catch (Exception ex) {
                Logger.getLogger(TipoParametro.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public interface ParametroSupplier {
        Parametro get(TipoParametro tipo);
    }
    
    public interface ParametroHandler {
        void handle(Parametro param);
    }
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Mapping {
        TipoParametro value();
    }
    
    @Getter
    @RequiredArgsConstructor
    enum TipoValor {
        VALOR(String.class, new Converter<String, Object>() {
            
            @Override
            public Object sourceToTarget(String source) {
                return source;
            }
            
            @Override
            public String targetToSource(Object target) {
                return target.toString();
            }
        }){
            
            @Override
            Object get(Parametro param) {
                return param.getValor();
            }

            @Override
            void set(Parametro param, Object val) {
                param.setValor((String) val);
            }
                        
        },
        ANEXO(byte[].class, new Converter<byte[], Object>() {
            private ObjectMapper om = new ObjectMapper();
            
            @AllArgsConstructor
            class Writable {
                String classname;
                String content;
            }
            
            @Override
            public Object sourceToTarget(byte[] source) {
                try{
                    Writable writable = om.readValue(new String(source), Writable.class);
                    return om.readValue(writable.content, Class.forName(writable.classname));
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            public byte[] targetToSource(Object target) {
                try{
                    om.writeValueAsBytes(new Writable(target.getClass().getName(), om.writeValueAsString(target)));
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }){
            
            @Override
            Object get(Parametro param) {
                return param.getAnexo();
            }

            @Override
            void set(Parametro param, Object val) {
                param.setAnexo((byte[]) val);
            }
                        
        };
        
        private final Class<?> dbType;
        private final Converter cvrt;
        
        abstract Object get(Parametro param);
        abstract void set(Parametro param, Object val);
        
        public Converter defaultConverter(){
            return cvrt;
        }
        
    }
}