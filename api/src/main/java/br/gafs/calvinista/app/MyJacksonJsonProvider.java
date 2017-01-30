/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
//@Produces({MediaType.APPLICATION_JSON})
//@Consumes(MediaType.APPLICATION_JSON)
//@Singleton
public class MyJacksonJsonProvider implements ContextResolver<ObjectMapper> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    
    static {
      MAPPER.setSerializationInclusion(Include.NON_EMPTY);
      MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
      MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
      MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      
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
}
