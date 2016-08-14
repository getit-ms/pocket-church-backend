/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

import br.gafs.calvinista.dto.CalvinEmailDTO;
import br.gafs.calvinista.dto.CalvinEmailDTO.Materia;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.util.MensagemUtil;
import br.gafs.util.email.EmailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void testaJSON() throws IOException{
        Igreja igreja = new Igreja();
        igreja.setNome("Igreja Teste");
        Institucional institucional = new Institucional(igreja);
        institucional.setSite("www.siteexemplo.com.br");
        MensagemEmailDTO email = MensagemUtil.email(institucional, "Igreja Presbiteriana Nacional - Novo Acesso",
                new CalvinEmailDTO(new CalvinEmailDTO.Manchete("Bem Vindo, Gabriel Silva", "Sua senha para acesso ao aplicativo Igreja Presbiteriana Nacional é 1234asdf. Após realizar o primeiro acesso lembre-se de fazer a troca para uma senha de sua escolha.",
                        "http://calvin.projetos-gafs.com", "Projetos Calvin"), Collections.EMPTY_LIST));
        
        ObjectMapper om = new ObjectMapper();
        System.out.println(om.writeValueAsString(email));
    }
    
    @Test
    public void testaVersiculoDiario(){
        
    }
}
