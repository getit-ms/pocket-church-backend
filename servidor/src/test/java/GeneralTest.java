/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import br.gafs.calvinista.entity.CalendarioAtendimento;
import br.gafs.calvinista.entity.HorarioAtendimento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Membro;
import br.gafs.util.date.DateUtil;
import br.gafs.util.email.EmailUtil;
import br.gafs.util.string.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.*;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gabriel
 */
public class GeneralTest {

    private static final long MILLIS_DAY = 1000 * 60 * 60 * 24;

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
    public void testaExtairTextoPDF() throws Exception {
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

                if (property != null && property.getMember() != null) {
                    if (property.getMember().hasAnnotation(Temporal.class)) {
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
            try {
                String value = jp.getValueAsString();
                if (!StringUtil.isEmpty(value)) {
                    switch (type) {
                        case DATE: {
                            return DateUtil.parseData(value.substring(0, 10), "yyyy-MM-dd");
                        }
                        case TIME: {
                            return DateUtil.parseData(value.substring(11, 16), "HH:mm");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return super.deserialize(jp, ctxt);
        }
    }

    @Test
    public void testaMultiplosTimeZones() throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSXX"));
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class,
                (JsonDeserializer) new DataDeserializerTimeZone());
        mapper.registerModule(module);

        String json = "{\"dataNascimento\":\"2017-02-06T12:00:00.000+0500\"}";

        TIME_ZONE_THREAD_LOCAL.set(TimeZone.getTimeZone("GMT-3:00"));
        System.out.println(mapper.readValue(json, Membro.class).getDataNascimento() + " " + TIME_ZONE_THREAD_LOCAL.get().getID());

        TIME_ZONE_THREAD_LOCAL.set(TimeZone.getTimeZone("America/Sao_Paulo"));
        System.out.println(mapper.readValue(json, Membro.class).getDataNascimento() + " " + TIME_ZONE_THREAD_LOCAL.get().getID());

        json = "{\"horaInicio\":\"2017-02-06T12:00:00.000-0500\"}";

        HorarioAtendimento hora = mapper.readValue(json, HorarioAtendimento.class);
        hora.setCalendario(new CalendarioAtendimento());
        hora.getCalendario().setIgreja(new Igreja());
        hora.getCalendario().getIgreja().setTimezone("GMT-3:00");
        System.out.println(hora.getHoraInicio() + " " + TimeZone.getTimeZone(hora.getCalendario().getIgreja().getTimezone()).getID());

        hora = mapper.readValue(json, HorarioAtendimento.class);
        hora.setCalendario(new CalendarioAtendimento());
        hora.getCalendario().setIgreja(new Igreja());
        hora.getCalendario().getIgreja().setTimezone("GMT-5:00");

        hora = mapper.readValue(json, HorarioAtendimento.class);
        hora.setCalendario(new CalendarioAtendimento());
        hora.getCalendario().setIgreja(new Igreja());
        hora.getCalendario().getIgreja().setTimezone("America/Sao_Paulo");
        System.out.println(hora.getHoraInicio() + " " + TimeZone.getTimeZone(hora.getCalendario().getIgreja().getTimezone()).getID());
    }

    @Test
    public void verificaTimeZone() {
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        cal.setTimeInMillis((System.currentTimeMillis() / MILLIS_DAY) * MILLIS_DAY);

        System.out.println(DateUtil.criarDataAtualSemHora(
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)
        ));
    }
}
