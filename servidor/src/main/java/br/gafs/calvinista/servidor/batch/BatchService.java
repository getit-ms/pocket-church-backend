package br.gafs.calvinista.servidor.batch;

import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.batch.dto.AgenteDTO;
import br.gafs.calvinista.servidor.batch.dto.ExecucaoServicoDTO;
import br.gafs.calvinista.servidor.batch.dto.LoginDTO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by Gabriel on 22/11/2018.
 */
@Singleton
public class BatchService {
    private static final String VERSAO = ResourceBundle.getBundle("versao", new Locale("pt-br")).getString("VERSAO");

    public static final Logger LOGGER = LogManager.getLogger(BatchService.class);

    private Client client = ClientBuilder.newBuilder().build();

    private static final String BASE_PATH = "https://batch.getitmobilesolutions.com/control";

    @EJB
    private ParametroService parametroService;

    private String tokenConexao;

    @PostConstruct
    @Schedule(hour = "*/12")
    public void autentica() {
        LOGGER.info("Preparando para autenticar para batch");

        this.tokenConexao = client.target(BASE_PATH).path("acesso/login")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(new LoginDTO(
                        (String) parametroService.get(Parametro.GLOBAL, TipoParametro.UUID_APP_BATCH),
                        (String) parametroService.get(Parametro.GLOBAL, TipoParametro.TOKEN_ACESSO_APP_BATCH),
                        VERSAO
                ))).readEntity(AgenteDTO.class).getTokenConexao();

        LOGGER.info("Autenticação batch realizada com sucesso");

    }

    @Asynchronous
    public void processaBoletim(String igreja, Long boletim) {

        executeService("processa-boletim", "Processamento de Boletim " + igreja + " " + boletim,
                entradas().set("IGREJA", igreja).set("BOLETIM", boletim.toString()));

    }

    private void executeService(String servico, String descricao, Map<String, List<String>> entradas) {

        LOGGER.info("Preparando para solicitar execução do serviço " + servico);

        ExecucaoServicoDTO execucaoServico = client.target(BASE_PATH).path("servico/" + servico + "/execute")
                .queryParam("descricao", descricao)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Agente " + tokenConexao)
                .post(Entity.json(entradas))
                .readEntity(ExecucaoServicoDTO.class);

        LOGGER.info("Serviço criado para execução batch com ID " + execucaoServico.getId());
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

    static class EntradasBuilder extends HashMap<String, List<String>> {

        public EntradasBuilder set(String chave, String... values) {
            put(chave, Arrays.asList(values));
            return this;
        }

    }
}
