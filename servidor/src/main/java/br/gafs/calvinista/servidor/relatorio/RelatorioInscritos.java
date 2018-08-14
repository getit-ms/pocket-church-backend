package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.FiltroInscricao;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioInscritos implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Evento evento;

    public RelatorioInscritos(Evento evento){
        this.igreja = evento.getIgreja();
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
        BuscaPaginadaDTO busca;
        List<InscricaoEvento> inscricoes = new ArrayList<InscricaoEvento>();
        final FiltroInscricaoDTO filtro = new FiltroInscricaoDTO(1, 30);
        do{
            busca = tool.transactional(new ProcessamentoService.ExecucaoTransacional<BuscaPaginadaDTO>() {
                @Override
                public BuscaPaginadaDTO execute(DAOService daoService) {
                    return daoService.findWith(new FiltroInscricao(evento.getId(),
                            igreja.getChave(), null, filtro));
                }
            });
            inscricoes.addAll(busca.getResultados());
            filtro.setPagina(filtro.getPagina() + 1);
        }while(busca.isHasProxima());

        return ReportUtil.igreja(
                "report/inscritos_evento.jasper",
                evento.getNome(),
                evento.getIgreja())
                .arg("EVENTO", evento)
                .arg("REPORT_LOCALE", new Locale(igreja.getLocale()))
                .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(igreja.getTimezone()))
                .collection(inscricoes).build();
    }
}
