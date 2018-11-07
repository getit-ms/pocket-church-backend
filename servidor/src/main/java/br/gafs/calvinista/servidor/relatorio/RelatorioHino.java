package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.entity.Hino;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Template;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioHino implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Template template;
    private Hino hino;

    public RelatorioHino(Igreja igreja, Template template, Hino hino){
        this.hino = hino;
        this.igreja = igreja;
        this.template = template;
    }

    @Override
    public String getId() {
        return hino.getId().toString();
    }

    @Override
    public String getTitulo() {
        return hino.getNumero() + " - " + hino.getNome();
    }

    @Override
    public String getFilename() {
        return hino.getFilename();
    }

    @Override
    public ReportUtil.ExporterImpl generate(final ProcessamentoService.ProcessamentoTool tool) {
        return ReportUtil.igreja(
                "report/hino.jasper",
                getTitulo(), igreja, template).bean(hino).build();
    }
}
