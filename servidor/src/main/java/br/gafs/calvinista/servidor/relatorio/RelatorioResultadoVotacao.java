package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dto.ResultadoVotacaoDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Template;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
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
public class RelatorioResultadoVotacao implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Template template;
    private ResultadoVotacaoDTO resultado;

    public RelatorioResultadoVotacao(ResultadoVotacaoDTO resultado, Template template) {
        this.igreja = resultado.getIgreja();
        this.template = template;
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
            return ReportUtil.igreja(
                            "report/resultado_votacao.jasper",
                            resultado.getNome(), igreja, template)
                    .arg("REPORT_CHART", new File(RelatorioResultadoVotacao.class.getResource("/report/resultado_votacao_grafico.jasper").toURI()).getAbsoluteFile())
                    .arg("REPORT_LOCALE", new Locale(igreja.getLocale()))
                    .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(igreja.getTimezone()))
                    .collection(resultado.getQuestoes()).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
