package br.gafs.pocket.corporate.servidor.relatorio;

import br.gafs.pocket.corporate.dao.FiltroInscricao;
import br.gafs.pocket.corporate.dto.FiltroInscricaoDTO;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.util.ReportUtil;
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
    private Empresa empresa;
    private Template template;
    private Evento evento;

    public RelatorioInscritos(Evento evento, Template template){
        this.empresa = evento.getEmpresa();
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
        BuscaPaginadaDTO busca;
        List<InscricaoEvento> inscricoes = new ArrayList<InscricaoEvento>();
        final FiltroInscricaoDTO filtro = new FiltroInscricaoDTO(null, null,  1, 30);
        do{
            busca = tool.transactional(new ProcessamentoService.ExecucaoTransacional<BuscaPaginadaDTO>() {
                @Override
                public BuscaPaginadaDTO execute(DAOService daoService) {
                    return daoService.findWith(new FiltroInscricao(evento.getId(),
                            empresa.getChave(), null, filtro));
                }
            });
            inscricoes.addAll(busca.getResultados());
            filtro.setPagina(filtro.getPagina() + 1);
        }while(busca.isHasProxima());

        return ReportUtil.empresa(
                "report/inscritos_evento.jasper",
                evento.getNome(),
                evento.getEmpresa(),
                template)
                .arg("EVENTO", evento)
                .arg("REPORT_LOCALE", new Locale(empresa.getLocale()))
                .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(empresa.getTimezone()))
                .collection(inscricoes).build();
    }
}
