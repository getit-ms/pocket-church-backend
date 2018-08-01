package br.gafs.pocket.corporate.servidor.relatorio;

import br.gafs.pocket.corporate.entity.Documento;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.util.ReportUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioDocumento implements ProcessamentoRelatorioCache.Relatorio {
    private Empresa empresa;
    private Documento documento;

    public RelatorioDocumento(Documento documento){
        this.documento = documento;
        this.empresa = documento.getEmpresa();
    }

    @Override
    public String getId() {
        return documento.getId().toString();
    }

    @Override
    public String getTitulo() {
        return documento.getTitulo();
    }

    @Override
    public String getFilename() {
        return documento.getFilename();
    }

    @Override
    public ReportUtil.ExporterImpl generate(final ProcessamentoService.ProcessamentoTool tool) {
        return ReportUtil.empresa(
                "report/documento.jasper",
                getTitulo(),
                empresa).bean(documento).build();
    }
}
