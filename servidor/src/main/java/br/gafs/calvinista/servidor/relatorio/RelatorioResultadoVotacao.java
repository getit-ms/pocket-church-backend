package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.FiltroInscricao;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.dto.ResultadoVotacaoDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioResultadoVotacao implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private ResultadoVotacaoDTO resultado;

    public RelatorioResultadoVotacao(ResultadoVotacaoDTO resultado){
        this.igreja = resultado.getIgreja();
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
    public ReportUtil.ExporterImpl generate(final DAOService daoService) {
        try {
            return ReportUtil.igreja(
                    "report/resultado_votacao.jasper",
                    resultado.getNome(),
                    igreja)
                    .arg("REPORT_CHART", new File(RelatorioResultadoVotacao.class.getResource("/report/resultado_votacao_grafico.jasper").toURI()).getAbsoluteFile())
                    .arg("REPORT_LOCALE", new Locale(igreja.getLocale()))
                    .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(igreja.getTimezone()))
                    .collection(resultado.getQuestoes()).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
