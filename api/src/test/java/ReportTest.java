
import br.gafs.calvinista.app.ReportBuilder;
import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.dto.ResultadoVotacaoDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Opcao;
import br.gafs.calvinista.entity.Questao;
import br.gafs.calvinista.entity.Votacao;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gabriel
 */
public class ReportTest {
    
//    @Test
    public void resultadoVotacaoTest() throws Exception {
        Igreja igreja = new Igreja();
        igreja.setNome("Igreja Teste");
        
        Votacao v = new Votacao();
        v.setNome("Votação teste");
        v.setDescricao("Essa é a votação teste");
        ResultadoVotacaoDTO dto = new ResultadoVotacaoDTO(v);
        
        Questao q = new Questao();
        q.setQuestao("Essa é a questão teste");
        
        Opcao o1 = new Opcao();
        o1.setOpcao("Opção 1");
        
        Opcao o2 = new Opcao();
        o2.setOpcao("Opção 2");
        
        dto.init(q).
                resultado(o1, 5).
                resultado(o2, 6);
        
        
        FileOutputStream fos = new FileOutputStream("teste.pdf");
        ReportBuilder.init(igreja, new ParametrosIgrejaDTO()).template("jasper/resultado_votacao.jasper").value(dto).build(fos);
    }
    
    @Test
    public void dateTest() throws ParseException{
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
      
        System.out.println(">> "+sdf.parse("1990-05-24T03:00:00.000Z"));
        System.out.println(">> "+sdf.parse("1990-05-24T03:00:00.000-0300"));
        System.out.println(">>" + sdf.format(new Date()));
        
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        System.out.println(">>" + sdf.format(new Date()));
    }
}
