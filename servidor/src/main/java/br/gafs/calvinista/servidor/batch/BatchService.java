package br.gafs.calvinista.servidor.batch;

import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.batch.dto.AgenteDTO;
import br.gafs.calvinista.servidor.batch.dto.ExecucaoServicoDTO;
import br.gafs.calvinista.servidor.batch.dto.LoginDTO;
import br.gafs.calvinista.servidor.rest.EasyRESTClient;
import br.gafs.calvinista.servidor.rest.EasyRESTREsponse;
import br.gafs.calvinista.view.View;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by Gabriel on 22/11/2018.
 */
@Singleton
public class BatchService {
    private static final String VERSAO = ResourceBundle.getBundle("versao", new Locale("pt-br")).getString("VERSAO");

    public static final Logger LOGGER = LogManager.getLogger(BatchService.class);

    private EasyRESTClient client = new EasyRESTClient("https://batch.getitmobilesolutions.com/control");

    @EJB
    private ParametroService parametroService;

    private String tokenConexao;

    @PostConstruct
    @Schedule(hour = "*/12", persistent = false)
    public void autentica() {
        try {
            LOGGER.info("Preparando para autenticar para batch");

            EasyRESTREsponse<AgenteDTO> response =
                    client.requestJSON("acesso/login")
                            .put(new LoginDTO(
                                    (String) parametroService.get(Parametro.GLOBAL, TipoParametro.UUID_APP_BATCH),
                                    (String) parametroService.get(Parametro.GLOBAL, TipoParametro.TOKEN_ACESSO_APP_BATCH),
                                    VERSAO
                            ), AgenteDTO.class);

            if (response.isSucesso()) {
                this.tokenConexao = response.getBody().getTokenConexao();

                LOGGER.info("Autenticação batch realizada com sucesso");
            } else {
                LOGGER.error("Não foi possível realizar a autenticação batch. " +
                        response.getStatus() + " " + response.getError());
            }
        } catch (Exception ex) {
            LOGGER.error("Não foi possível realizar a autenticação batch. ", ex);
        }
    }

    @Asynchronous
    public void processaBoletim(String igreja, Long boletim) {

        executeService("processa-boletim", "Processamento de Boletim " + igreja + " " + boletim,
                entradas().set("IGREJA", igreja).set("BOLETIM", boletim.toString()));

    }

    private void executeService(String servico, String descricao, Map<String, List<String>> entradas) {

        LOGGER.info("Preparando para solicitar execução do serviço " + servico);

        EasyRESTREsponse<ExecucaoServicoDTO> response = client.requestJSON("servico/{0}/execute", servico)
                .queryParam("descricao", descricao)
                .header("Authorization", "Agente " + tokenConexao)
                .post(entradas, ExecucaoServicoDTO.class);
        if (response.isSucesso()) {
            ExecucaoServicoDTO execucaoServico = response.getBody();

            LOGGER.info("Serviço criado para execução batch com ID " + execucaoServico.getId());
        } else {
            LOGGER.warn("Não foi possível executar o serviço batch: " +
                    response.getStatus() + " " + response.getError());
        }

    }

    @Asynchronous
    public void processaCifra(String igreja, Long cifra) {

        executeService("processa-cifra", "Processamento de Cifra " + igreja + " " + cifra,
                entradas().set("IGREJA", igreja).set("CIFRA", cifra.toString()));

    }

    @Asynchronous
    public void processaEstudo(String igreja, Long estudo) {

        executeService("processa-estudo", "Processamento de Estudo " + igreja + " " + estudo,
                entradas().set("IGREJA", igreja).set("ESTUDO", estudo.toString()));

    }

    EntradasBuilder entradas() {
        return new EntradasBuilder();
    }

    @Asynchronous
    public void processaDevocional(String igreja, Long devocional) {
        executeService("processa-devocional", "Processamento de Devocional " + igreja + " " + devocional,
                entradas().set("IGREJA", igreja).set("DEVOCIONAL", devocional.toString()));
    }

    static class EntradasBuilder extends HashMap<String, List<String>> {

        public EntradasBuilder set(String chave, String... values) {
            put(chave, Arrays.asList(values));
            return this;
        }

    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class MyJacksonJsonProvider implements ContextResolver<ObjectMapper> {

        private static final ObjectMapper MAPPER = new ObjectMapper();
        public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";

        static {
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
            MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            SimpleModule module = new SimpleModule();
            module.addDeserializer(Date.class, new CustomDateDeserializer());
            module.addSerializer(Date.class, new CustomDateSerializer());
            MAPPER.registerModule(module);

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getDefault());

            MAPPER.setDateFormat(sdf);
            MAPPER.setTimeZone(TimeZone.getDefault());
        }

        public MyJacksonJsonProvider() {
            System.out.println("Instantiate MyJacksonJsonProvider");
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            System.out.println("MyJacksonProvider.getContext() called with type: " + type);
            return MAPPER;
        }

        public static class CustomDateDeserializer extends DateDeserializers.DateDeserializer implements ContextualDeserializer, Cloneable {

            private TemporalType type = TemporalType.TIMESTAMP;

            @Override
            public CustomDateDeserializer createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
                try {
                    this.type = TemporalType.TIMESTAMP;

                    if (property != null && property.getMember() != null) {
                        if (property.getMember().hasAnnotation(Temporal.class)) {
                            this.type = property.getMember().getAnnotation(Temporal.class).value();
                        } else if (property.getMember().hasAnnotation(View.JsonTemporal.class)) {
                            this.type = property.getMember().getAnnotation(View.JsonTemporal.class).value().getType();
                        }
                    }

                    return (CustomDateDeserializer) this.clone();
                } catch (CloneNotSupportedException ex) {
                    java.util.logging.Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
                    return this;
                }
            }

            @Override
            public Date deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                try {
                    String value = jp.getValueAsString();
                    if (!StringUtil.isEmpty(value)) {
                        SimpleDateFormat sdf = new SimpleDateFormat();
                        sdf.applyPattern(DATE_FORMAT);

                        if (value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}.{1,6}")) {
                            sdf.setTimeZone(TimeZone.getDefault());
                            switch (type) {
                                case DATE: {
                                    sdf.applyPattern("yyyy-MM-dd");
                                    return sdf.parse(value.substring(0, 10));
                                }
                                case TIME: {
                                    sdf.applyPattern("HH:mm");
                                    return sdf.parse(value.substring(11, 16));
                                }
                            }

                            return sdf.parse(value);
                        } else if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            sdf.applyPattern("yyyy-MM-dd");
                            return sdf.parse(value);
                        } else if (value.matches("\\d{2}:\\d{2}")) {
                            sdf.applyPattern("HH:mm");
                            return sdf.parse(value);
                        }

                        throw new RuntimeException("Não foi possível processar a requisição.");
                    }
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("Não foi possível processar a requisição.");
                }

                return null;
            }
        }

        public static class CustomDateSerializer extends DateSerializer implements ContextualSerializer, Cloneable {

            private TemporalType type = TemporalType.TIMESTAMP;

            @Override
            public CustomDateSerializer createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
                try {
                    this.type = TemporalType.TIMESTAMP;

                    if (property != null && property.getMember() != null) {
                        if (property.getMember().hasAnnotation(Temporal.class)) {
                            this.type = property.getMember().getAnnotation(Temporal.class).value();
                        } else if (property.getMember().hasAnnotation(View.JsonTemporal.class)) {
                            this.type = property.getMember().getAnnotation(View.JsonTemporal.class).value().getType();
                        }
                    }

                    return (CustomDateSerializer) this.clone();
                } catch (CloneNotSupportedException ex) {
                    java.util.logging.Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
                    return this;
                }
            }

            @Override
            public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonGenerationException {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern(DATE_FORMAT);

                try {
                    if (value != null) {
                        sdf.setTimeZone(TimeZone.getDefault());
                        switch (type) {
                            case DATE: {
                                sdf.applyPattern("yyyy-MM-dd");
                                gen.writeString(sdf.format(value));
                                return;
                            }
                            case TIME: {
                                sdf.applyPattern("HH:mm");
                                gen.writeString(sdf.format(value));
                                return;
                            }
                        }

                    }
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
                }

                gen.writeString(sdf.format(value));
            }
        }
    }
}
