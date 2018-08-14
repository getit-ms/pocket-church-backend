package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.entity.Igreja;
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
public class RelatorioEstudo implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Estudo estudo;

    public RelatorioEstudo(Estudo estudo){
        this.estudo = estudo;
        this.igreja = estudo.getIgreja();
    }

    @Override
    public String getId() {
        return estudo.getId().toString();
    }

    @Override
    public String getTitulo() {
        return estudo.getTitulo();
    }

    @Override
    public String getFilename() {
        return estudo.getFilename();
    }

    @Override
    public ReportUtil.ExporterImpl generate(final ProcessamentoService.ProcessamentoTool tool) {
        return ReportUtil.igreja(
                "report/estudo.jasper",
                getTitulo(),
                igreja).bean(estudo).build();
    }
}
