package br.gafs.pocket.corporate.servidor.relatorio;

import br.gafs.pocket.corporate.dto.ResultadoEnqueteDTO;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.util.ReportUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioResultadoEnquete implements ProcessamentoRelatorioCache.Relatorio {
    private Empresa empresa;
    private ResultadoEnqueteDTO resultado;

    public RelatorioResultadoEnquete(ResultadoEnqueteDTO resultado){
        this.empresa = resultado.getEmpresa();
        this.resultado = resultado;
    }

    @Override
    public String getId() {
        return resultado.getId().toString();
    }

    @Override
    public String getTitulo() {
        return resultado.getNome();
    }

    @Override
    public String getFilename() {
        return resultado.getNome();
    }

    @Override
    public ReportUtil.ExporterImpl generate(final ProcessamentoService.ProcessamentoTool tool) {
        try {
            return ReportUtil.empresa(
                    "report/resultado_enquete.jasper",
                    resultado.getNome(),
                    empresa)
                    .arg("REPORT_CHART", new File(RelatorioResultadoEnquete.class.getResource("/report/resultado_enquete_grafico.jasper").toURI()).getAbsoluteFile())
                    .arg("REPORT_LOCALE", new Locale(empresa.getLocale()))
                    .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(empresa.getTimezone()))
                    .collection(resultado.getQuestoes()).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
