package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.entity.Hino;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.DAOService;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioHino implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Hino hino;

    public RelatorioHino(Igreja igreja, Hino hino){
        this.hino = hino;
        this.igreja = igreja;
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
    public ReportUtil.ExporterImpl generate(final DAOService daoService) {
        return ReportUtil.igreja(
                "report/hino.jasper",
                getTitulo(),
                igreja).bean(hino).build();
    }
}
