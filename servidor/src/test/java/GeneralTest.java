/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

import br.gafs.pocket.corporate.dto.CalvinEmailDTO;
import br.gafs.pocket.corporate.dto.MensagemEmailDTO;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.util.MensagemUtil;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.management.Query.value;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Gabriel
 */
public class GeneralTest {
    
    public GeneralTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
//    @Test
    public void hello() throws InterruptedException, FileNotFoundException {
        EmailUtil.sendMail("E-mail teste", "Assunto teste", Arrays.asList("gafsel@gmail.com"));
        
        Thread.sleep(60000);
    }
    
//    @Test
    public void testaJSON() throws IOException, InterruptedException{
        Empresa empresa = new Empresa();
        empresa.setChave("tst");
        empresa.setNome("Empresa Presbiteriana de Tambaú");
        Institucional institucional = new Institucional(empresa);
        institucional.setSite("https://getitmobilesolutions.com");
        MensagemEmailDTO email = MensagemUtil.email(institucional, "Empresa Presbiteriana Nacional - Novo Acesso",
                new CalvinEmailDTO(new CalvinEmailDTO.Manchete("Bem Vindo, Gabriel Silva", "Sua senha para acesso ao aplicativo Empresa Presbiteriana Nacional é 1234asdf. Após realizar o primeiro acesso lembre-se de fazer a troca para uma senha de sua escolha.",
                        "https://getitmobilesolutions.com", "Projetos Calvin"), Collections.EMPTY_LIST));
        
        EmailUtil.sendMail(email.getMessage(), email.getSubject(), Arrays.asList("gafsel@gmail.com"), email.getDataSources(), email.getAttachmentsNames());
        Thread.sleep(10000);
    }
    
//    @Test
    public void testaExtairTextoPDF() throws Exception{
        PDDocument pdffile = PDDocument.load(GeneralTest.class.getResourceAsStream("/p02.pdf"));
        
        StringWriter writer = new StringWriter();
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.writeText(pdffile, writer);
        System.out.println(writer.toString());
    }
    
    public static final ThreadLocal<TimeZone> TIME_ZONE_THREAD_LOCAL = new ThreadLocal<TimeZone>() {
        @Override
        protected TimeZone initialValue() {
            return TimeZone.getDefault();
        }
    };
    
    public static class DataDeserializerTimeZone extends DateDeserializers.DateDeserializer implements ContextualDeserializer, Cloneable {

        private TemporalType type = TemporalType.TIMESTAMP;

        @Override
        public DataDeserializerTimeZone createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            try {
                this.type = TemporalType.TIMESTAMP;
                
                if (property != null && property.getMember() != null){
                    if (property.getMember().hasAnnotation(Temporal.class)){
                        this.type = property.getMember().getAnnotation(Temporal.class).value();
                    }
                }
                
                return (DataDeserializerTimeZone) this.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(GeneralTest.class.getName()).log(Level.SEVERE, null, ex);
                return this;
            }
        }
        
        @Override
        public Date deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            try{
                String value = jp.getValueAsString();
                if (!StringUtil.isEmpty(value)){
                    switch (type){
                        case DATE:{
                            return DateUtil.parseData(value.substring(0, 10), "yyyy-MM-dd");
                        }
                        case TIME:{
                            return DateUtil.parseData(value.substring(11, 16), "HH:mm");
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            
            return super.deserialize(jp, ctxt);
        }
    }
    
    @Test
    public void testaMultiplosTimeZones() throws JsonProcessingException, IOException{
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSXX"));
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class,
                (JsonDeserializer) new DataDeserializerTimeZone());
        mapper.registerModule(module);

        String json = "{\"dataNascimento\":\"2017-02-06T12:00:00.000+0500\"}";
        
        TIME_ZONE_THREAD_LOCAL.set(TimeZone.getTimeZone("GMT-3:00"));
        System.out.println(mapper.readValue(json, Colaborador.class).getDataNascimento() + " " + TIME_ZONE_THREAD_LOCAL.get().getID());
        
        TIME_ZONE_THREAD_LOCAL.set(TimeZone.getTimeZone("America/Sao_Paulo"));
        System.out.println(mapper.readValue(json, Colaborador.class).getDataNascimento() + " " + TIME_ZONE_THREAD_LOCAL.get().getID());

        json = "{\"horaInicio\":\"2017-02-06T12:00:00.000-0500\"}";
        
        HorarioAtendimento hora = mapper.readValue(json, HorarioAtendimento.class);
        hora.setCalendario(new CalendarioAtendimento());
        hora.getCalendario().setEmpresa(new Empresa());
        hora.getCalendario().getEmpresa().setTimezone("GMT-3:00");
        System.out.println(hora.getHoraInicio() + " " + TimeZone.getTimeZone(hora.getCalendario().getEmpresa().getTimezone()).getID());

        hora = mapper.readValue(json, HorarioAtendimento.class);
        hora.setCalendario(new CalendarioAtendimento());
        hora.getCalendario().setEmpresa(new Empresa());
        hora.getCalendario().getEmpresa().setTimezone("GMT-5:00");

        hora = mapper.readValue(json, HorarioAtendimento.class);
        hora.setCalendario(new CalendarioAtendimento());
        hora.getCalendario().setEmpresa(new Empresa());
        hora.getCalendario().getEmpresa().setTimezone("America/Sao_Paulo");
        System.out.println(hora.getHoraInicio() + " " + TimeZone.getTimeZone(hora.getCalendario().getEmpresa().getTimezone()).getID());
    }
}
