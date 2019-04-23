/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.entity.domain;

import br.gafs.calvinista.entity.Parametro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
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

    // Integração PagSeguro
    USER_PAGSEGURO(TipoValor.VALOR, String.class, null),
    TOKEN_PAGSEGURO(TipoValor.VALOR, String.class, null),
    HABILITADO_PAGSEGURO(TipoValor.VALOR, boolean.class, "false"),
    ITEM_INSCRICAO_PAGSEGURO(TipoValor.VALOR, String.class, "Inscrição em {0} para {1}"),

    // Mensagens de Push Notification Automático
    PUSH_TITLE_VERSICULO_DIARIO(TipoValor.VALOR, String.class, "Palavra de Deus"),
    PUSH_TITLE_LEMBRETE_LEITURA_BIBLICA(TipoValor.VALOR, String.class, "Leitura Bíblica de hoje!"),
    PUSH_TITLE_ANIVERSARIO(TipoValor.VALOR, String.class, "HOJE É UM DIA ESPECIAL!"),
    PUSH_BODY_ANIVERSARIO(TipoValor.VALOR, String.class, "Olá, {0}.\nNo dia do seu aniversário queremos desejar a você muitas bençãos de Deus, junto aos seus queridos. Parabéns! Felicidades!\nSão os votos da sua {1}."),
    PUSH_TITLE_PUBLICACAO(TipoValor.VALOR, String.class, "Nova Publicação!"),
    PUSH_BODY_PUBLICACAO(TipoValor.VALOR, String.class, "Veja agora a nova publicação ''{1}'' da {0}."),
    PUSH_TITLE_BOLETIM(TipoValor.VALOR, String.class, "Novo Boletim!"),
    PUSH_BODY_BOLETIM(TipoValor.VALOR, String.class, "Veja agora a nova edição do Boletim ''{1}'' da {0}."),
    PUSH_TITLE_ESTUDO(TipoValor.VALOR, String.class, "Novo Estudo!"),
    PUSH_BODY_ESTUDO(TipoValor.VALOR, String.class, "Veja agora o novo Estudo ''{1}'' > ''{2}'' da {0}."),
    PUSH_TITLE_NOTICIA(TipoValor.VALOR, String.class, "Notícia!"),
    PUSH_BODY_NOTICIA(TipoValor.VALOR, String.class, "Veja agora a notícia ''{1}'' publicada por {0}."),
    PUSH_TITLE_YOUTUBE_AO_VIVO(TipoValor.VALOR, String.class, "Estamos AO VIVO!"),
    PUSH_BODY_YOUTUBE_AO_VIVO(TipoValor.VALOR, String.class, "Assista ''{0}'' ao vivo agora!"),
    PUSH_TITLE_YOUTUBE_AGENDADO(TipoValor.VALOR, String.class, "Estaremos AO VIVO hoje {1}!"),
    PUSH_BODY_YOUTUBE_AGENDADO(TipoValor.VALOR, String.class, "Assista ''{0}'' ao vivo hoje as {1}."),
    PUSH_TITLE_FACEBOOK_AO_VIVO(TipoValor.VALOR, String.class, "Estamos AO VIVO!"),
    PUSH_BODY_FACEBOOK_AO_VIVO(TipoValor.VALOR, String.class, "Assista ''{0}'' ao vivo agora!"),
    PUSH_TITLE_FACEBOOK_AGENDADO(TipoValor.VALOR, String.class, "Estaremos AO VIVO hoje {1}!"),
    PUSH_BODY_FACEBOOK_AGENDADO(TipoValor.VALOR, String.class, "Assista ''{0}'' ao vivo hoje as {1}."),
    PUSH_TITLE_CONFIRMACAO_AGENDAMENTO(TipoValor.VALOR, String.class, "Confirmação de Aconselhamento"),
    PUSH_BODY_CONFIRMACAO_AGENDAMENTO(TipoValor.VALOR, String.class, "O pastor {0} confirmou seu aconselhamento no dia {1} de {2} a {3}."),
    PUSH_TITLE_CANCELAMENTO_AGENDAMENTO(TipoValor.VALOR, String.class, "Cancelamento de Aconselhamento"),
    PUSH_BODY_CANCELAMENTO_AGENDAMENTO(TipoValor.VALOR, String.class, "{0} cancelou o aconselhamento do dia {1} de {2} a {3}."),
    PUSH_TITLE_CANCELAMENTO_AGENDAMENTO_MEMBRO(TipoValor.VALOR, String.class, "Cancelamento de Aconselhamento"),
    PUSH_BODY_CANCELAMENTO_AGENDAMENTO_MEMBRO(TipoValor.VALOR, String.class, "{0} cancelou o aconselhamento do dia {1} de {2} a {3}."),
    PUSH_TITLE_AGENDAMENTO(TipoValor.VALOR, String.class, "Solicitação de Aconselhamento"),
    PUSH_BODY_AGENDAMENTO(TipoValor.VALOR, String.class, "{0} deseja marcar um aconselhamento no dia {1} de {2} a {3}."),
    PUSH_TITLE_NOTIFICACAO(TipoValor.VALOR, String.class, "Notificação {0}!"),
    PUSH_TITLE_ATENDIMENTO_PEDIDO_ORACAO(TipoValor.VALOR, String.class, "Pedido de Oração"),
    PUSH_BODY_ATENDIMENTO_PEDIDO_ORACAO(TipoValor.VALOR, String.class, "Seu pedido de oração solicitado em {0} foi encaminhado internamente."),

    // Integração Push Notifications
    PUSH_ANDROID_KEY(TipoValor.VALOR, String.class, null),
    PUSH_ANDROID_SENDER_ID(TipoValor.VALOR, String.class, null),
    PUSH_IOS_PASS(TipoValor.VALOR, String.class, null),
    PUSH_IOS_CERTIFICADO(TipoValor.ANEXO, byte[].class, null),
    IPB_PUSH_TOKEN(TipoValor.VALOR, String.class, null),

    // Integrações Google
    GOOGLE_OAUTH_CLIENT_KEY(TipoValor.VALOR, String.class, null),
    GOOGLE_OAUTH_SECRET_KEY(TipoValor.VALOR, String.class, null),
    YOUTUBE_CHANNEL_ID(TipoValor.VALOR, String.class, null),
    GOOGLE_CALENDAR_ID(TipoValor.ANEXO, List.class, null),

    // Integração Flickr
    FLICKR_OAUTH_CLIENT_KEY(TipoValor.VALOR, String.class, null),
    FLICKR_OAUTH_SECRET_KEY(TipoValor.VALOR, String.class, null),
    FLICKR_ID(TipoValor.VALOR, String.class, null),

    // Integração Facebook
    FACEBOOK_PAGE_ID(TipoValor.VALOR, String.class, null),
    FACEBOOK_APP_ID(TipoValor.VALOR, String.class, "2260125654205962"),
    FACEBOOK_APP_SECRET(TipoValor.VALOR, String.class, "3f2773a0bb27c75363009a903b747d30"),
    FACEBOOK_APP_CODE(TipoValor.ANEXO, String.class, null),

    // Senha de tokens JWT
    JWT_KEY_ALGORITHM(TipoValor.VALOR, String.class, "HmacSHA512"),
    JWT_KEY(TipoValor.VALOR, String.class, "wPDBZCfB6Md1JOpCMXeCPFmCvKP+WGeJrWWL+8jS+r6bUaFU23WKJv0xU6pwXKsadCIXo1z/AhsNMkhQWIa2Tg=="),

    // Formats
    FORMATO_DATA(TipoValor.VALOR, String.class, "dd/MM/yyyy"),
    FORMATO_DATA_HORA(TipoValor.VALOR, String.class, "dd/MM/yyyy HH:mm"),
    FORMATO_HORA(TipoValor.VALOR, String.class, "HH:mm"),

    // Template de E-mails
    EMAIL_SUBJECT_SOLICITAR_REDEFINICAO_SENHA(TipoValor.VALOR, String.class, "Solicitação de Redefinição de Senha"),
    EMAIL_BODY_SOLICITAR_REDEFINICAO_SENHA(TipoValor.ANEXO, String.class, null),
    EMAIL_SUBJECT_REDEFINIR_SENHA(TipoValor.VALOR, String.class, "Nova Senha"),
    EMAIL_BODY_REDEFINIR_SENHA(TipoValor.ANEXO, String.class, null),
    EMAIL_SUBJECT_PAGAMENTO_INSCRICAO(TipoValor.VALOR, String.class, "Inscrição em Evento"),
    EMAIL_BODY_PAGAMENTO_INSCRICAO(TipoValor.ANEXO, String.class, null),
    EMAIL_SUBJECT_DAR_ACESSO(TipoValor.VALOR, String.class, "Primeiro Acesso"),
    EMAIL_BODY_DAR_ACESSO(TipoValor.ANEXO, String.class, null),
    EMAIL_SUBJECT_CONFIRMAR_INSCRICAO(TipoValor.VALOR, String.class, "Confirmação de Inscrição em Evento"),
    EMAIL_BODY_CONFIRMAR_INSCRICAO(TipoValor.ANEXO, String.class, null),
    EMAIL_SUBJECT_PC_IPB_RESPOSTA(TipoValor.VALOR, String.class, "IPB App - Pocket Church"),
    EMAIL_BODY_PC_IPB_RESPOSTA(TipoValor.ANEXO, String.class, null),
    EMAIL_SUBJECT_CONTATO_SITE_RESPOSTA(TipoValor.VALOR, String.class, "Contato GETIT"),
    EMAIL_BODY_CONTATO_SITE_RESPOSTA(TipoValor.ANEXO, String.class, null),

    // Configurações SMTP
    SMTP_PORTA(TipoValor.VALOR, Integer.class, "587"),
    SMTP_ENABLE_START_TLS(TipoValor.VALOR, Boolean.class, "true"),
    SMTP_AUTH(TipoValor.VALOR, Boolean.class, "true"),
    SMTP_PROPERTIES(TipoValor.ANEXO, String.class, ("# CONFIGURA\\u00c7\\u00d5ES DE ENVIO DE EMAILS =====================================\n" +
            "# emails dos administradores separados por v\\u00edrgula\n" +
            "mail.smtp.adms = suporte@getitmobilesolutions.com\n" +
            "# define protocolo de envio como SMTP\n" +
            "mail.transport.protocol = smtp\n" +
            "#mail.smtp.starttls.enable -  deve ser setado para false quando n\\u00e3o estiver usando autenticacao\n" +
            "mail.smtp.starttls.enable = true\n" +
            "#mail.smtp.ssl.enable = true \n" +
            "\n" +
            "mail.smtp.host = email-smtp.us-east-1.amazonaws.com\n" +
            "\n" +
            "# ativa autenticacao\n" +
            "mail.smtp.auth = true\n" +
            "\n" +
            "mail.smtp.user = AKIAIQXFUIZAY6FEIX7Q\n" +
            "mail.smtp.pass = AgZwoq7BjUx+FpsSNQ8ng5YlsmS48jQMtZjMnJejVyh6\n" +
            "\n" +
            "mail.debug = true\n" +
            "# porta\n" +
            "mail.smtp.port = 587\n" +
            "# mesma porta para o socket\n" +
            "mail.smtp.socketFactory.port = 587\n" +
            "#confiara no servidor mesmo que n\\u00e3o esteja com certificado\n" +
            "mail.smtp.ssl.trust = *\n" +
            "mail.smtp.socketFactory.fallback = false\n" +
            "# ======================================================================\n" +
            "\n").getBytes()),
    ADMIN_MAILS(TipoValor.VALOR, String.class, "suporte@getitmobilesolutions.com"),
    SMTP_USERNAME(TipoValor.VALOR, String.class, "AKIAIQXFUIZAY6FEIX7Q"),
    SMTP_PASSWORD(TipoValor.VALOR, String.class, "AgZwoq7BjUx+FpsSNQ8ng5YlsmS48jQMtZjMnJejVyh6"),
    SMTP_FROM_NAME(TipoValor.VALOR, String.class, "Pocket Church"),
    SMTP_FROM_EMAIL(TipoValor.VALOR, String.class, "donotreply@getitmobilesolutions.com"),

    // Assets WEB
    BUNDLE_WEB(TipoValor.ANEXO, byte[].class, "{}".getBytes()),

    // Acesso Batch
    UUID_APP_BATCH(TipoValor.VALOR, String.class, "29f1dad0-64ea-4bce-8554-6a17aadeb7c0"),
    TOKEN_ACESSO_APP_BATCH(TipoValor.VALOR, String.class, "fe0b2a7d-1a19-4ef0-890c-a0226f9ceb06");

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
        converters.get(byte[].class).put(String.class, new Converter<byte[], String>(){

            @Override
            public String sourceToTarget(byte[] source) {
                return new String(source);
            }

            @Override
            public byte[] targetToSource(String target) {
                return target.getBytes();
            }

        });
    }
    
    public <T> T get(Parametro param) {
        Object valor = tipoValor.get(param);

        if (valor != null) {
            return (T) converter().sourceToTarget(valor);
        }

        if (defaultValue != null) {
            return (T) converter().sourceToTarget(defaultValue);
        }

        return null;
    }
    
    public <T> void set(Parametro param, T valor) {
        if (valor == null) {
            tipoValor.set(param, null);
        } else {
            tipoValor.set(param, converter().targetToSource(valor));
        }
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

            @Override
            public Object sourceToTarget(byte[] source) {
                try{
                    if (source != null) {
                        Writable writable = om.readValue(source, Writable.class);
                        return om.readValue(writable.content, Class.forName(writable.classname));
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public byte[] targetToSource(Object target) {
                try{
                    return om.writeValueAsBytes(new Writable(target.getClass().getName(), om.writeValueAsString(target)));
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Writable {
        String classname;
        String content;
    }
}