/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.app;

import br.gafs.pocket.corporate.view.View;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class MyJacksonJsonProvider implements ContextResolver<ObjectMapper> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    
    static {
        MAPPER.setSerializationInclusion(Include.NON_EMPTY);
        MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new CustomDateDeserializer());
        module.addSerializer(Date.class,new CustomDateSerializer());
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
        System.out.println("MyJacksonProvider.getContext() called with type: "+type);
        return MAPPER;
    }
    
    public static class CustomDateDeserializer extends DateDeserializers.DateDeserializer implements ContextualDeserializer, Cloneable {
        
        private TemporalType type = TemporalType.TIMESTAMP;
        
        @Override
        public CustomDateDeserializer createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            try {
                this.type = TemporalType.TIMESTAMP;
                
                if (property != null && property.getMember() != null){
                    if (property.getMember().hasAnnotation(Temporal.class)){
                        this.type = property.getMember().getAnnotation(Temporal.class).value();
                    }else if (property.getMember().hasAnnotation(View.JsonTemporal.class)){
                        this.type = property.getMember().getAnnotation(View.JsonTemporal.class).value().getType();
                    }
                }
                
                return (CustomDateDeserializer) this.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
                return this;
            }
        }
        
        @Override
        public Date deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            try{
                String value = jp.getValueAsString();
                if (!StringUtil.isEmpty(value)){
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    sdf.applyPattern(DATE_FORMAT);
                    
                    if (value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}.{1,6}")){
                        sdf.setTimeZone(TimeZone.getDefault());
                        switch (type){
                            case DATE:{
                                sdf.applyPattern("yyyy-MM-dd");
                                return sdf.parse(value.substring(0, 10));
                            }
                            case TIME:{
                                sdf.applyPattern("HH:mm");
                                return sdf.parse(value.substring(11, 16));
                            }
                        }
                        
                        return sdf.parse(value);
                    }else if (value.matches("\\d{4}-\\d{2}-\\d{2}")){
                        sdf.applyPattern("yyyy-MM-dd");
                        return sdf.parse(value);
                    }else if (value.matches("\\d{2}:\\d{2}")){
                        sdf.applyPattern("HH:mm");
                        return sdf.parse(value);
                    }
                    
                    throw new RuntimeException("Não foi possível processar a requisição.");
                }
            }catch(Exception ex){
                Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
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
                
                if (property != null && property.getMember() != null){
                    if (property.getMember().hasAnnotation(Temporal.class)){
                        this.type = property.getMember().getAnnotation(Temporal.class).value();
                    }else if (property.getMember().hasAnnotation(View.JsonTemporal.class)){
                        this.type = property.getMember().getAnnotation(View.JsonTemporal.class).value().getType();
                    }
                }
                
                return (CustomDateSerializer) this.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
                return this;
            }
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonGenerationException {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(DATE_FORMAT);
            
            try{
                if (value != null){
                    sdf.setTimeZone(TimeZone.getDefault());
                    switch (type){
                        case DATE:{
                            sdf.applyPattern("yyyy-MM-dd");
                            gen.writeString(sdf.format(value));
                            return;
                        }
                        case TIME:{
                            sdf.applyPattern("HH:mm");
                            gen.writeString(sdf.format(value));
                            return;
                        }
                    }
                    
                }
            }catch(Exception ex){
                Logger.getLogger(MyJacksonJsonProvider.class.getName()).log(Level.SEVERE, null, ex);
            }

            gen.writeString(sdf.format(value));
        }
    }
}
