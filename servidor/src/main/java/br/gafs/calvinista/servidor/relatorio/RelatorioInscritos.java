package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.FiltroInscricao;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.Template;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.servidor.relatorio.dataSource.JRPaginatedDataSource;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioInscritos implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Template template;
    private Evento evento;

    public RelatorioInscritos(Evento evento, Template template) {
        this.igreja = evento.getIgreja();
        this.template = template;
        this.evento = evento;
    }

    @Override
    public String getId() {
        return evento.getId().toString();
    }

    @Override
    public String getTitulo() {
        return evento.getNome();
    }

    @Override
    public String getFilename() {
        return evento.getFilename();
    }

    @Override
    public ReportUtil.ExporterImpl generate(final ProcessamentoService.ProcessamentoTool tool) {
        return ReportUtil.igreja(
                "report/inscritos_evento.jasper",
                evento.getNome(),
                igreja,
                template)
                .arg("EVENTO", evento)
                .arg("REPORT_LOCALE", new Locale(igreja.getLocale()))
                .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(igreja.getTimezone()))
                .dataSource(new JRPaginatedDataSource<>(
                        new JRPaginatedDataSource.Searcher<InscricaoEvento>() {
                            @Override
                            public BuscaPaginadaDTO<InscricaoEvento> search(final Integer pagina) {
                                return tool.transactional(new ProcessamentoService.ExecucaoTransacional<BuscaPaginadaDTO<InscricaoEvento>>() {
                                    @Override
                                    public BuscaPaginadaDTO<InscricaoEvento> execute(DAOService daoService) {
                                        return daoService.findWith(new FiltroInscricao(evento.getId(),
                                                igreja.getChave(), null, null, new FiltroInscricaoDTO(null, null, pagina, 30)));
                                    }
                                });
                            }
                        }
                )).build();
    }
}
