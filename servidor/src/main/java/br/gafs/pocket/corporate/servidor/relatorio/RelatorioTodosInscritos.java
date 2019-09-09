package br.gafs.pocket.corporate.servidor.relatorio;

import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.InscricaoEvento;
import br.gafs.pocket.corporate.entity.domain.TipoEvento;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.util.ReportUtil;
import br.gafs.dao.DAOService;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioTodosInscritos implements ProcessamentoRelatorioCache.Relatorio {

    private final static WritableCellFormat FONTE_TEXTO = new WritableCellFormat(
            new WritableFont(WritableFont.TIMES, 10));

    private Empresa empresa;
    private TipoEvento tipo;

    public RelatorioTodosInscritos(Empresa empresa, TipoEvento tipo){
        this.empresa = empresa;
        this.tipo = tipo;
    }

    @Override
    public String getId() {
        return empresa.getId().toString();
    }

    @Override
    public String getTitulo() {
        return "Inscritos em " + tipo.name() + " em " + empresa.getNome();
    }

    @Override
    public String getFilename() {
        return "Inscritos em " + tipo.name() + " em " + empresa.getNome();
    }

    @Override
    public ReportUtil.Reporter generate(final ProcessamentoService.ProcessamentoTool tool) {
        final List<InscricaoEvento> inscricoes = tool.transactional(new ProcessamentoService.ExecucaoTransacional<List<InscricaoEvento>>() {
            @Override
            public List<InscricaoEvento> execute(DAOService daoService) {
                return daoService.findWith(QueryAdmin.
                        INSCRICOES_EVENTOS_ATIVOS.create(tipo, empresa.getChave()));
            }
        });

        return new ReportUtil.Reporter(){

            @Override
            public void export(String tipo, OutputStream os) {
                try {
                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale("pt", "BR"));

                    WritableWorkbook workbook = Workbook.createWorkbook(os, wbSettings);

                    WritableSheet excelSheet = workbook.createSheet("Inscritos " + RelatorioTodosInscritos.this.tipo.name(), 0);

                    createLinhas(excelSheet, inscricoes);

                    workbook.write();
                    workbook.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    private void createLinhas(WritableSheet sheet, List<InscricaoEvento> inscricoes) throws WriteException {
        int i = 0;
        for (InscricaoEvento inscricao : inscricoes) {
            addLabel(sheet, 0, i, inscricao.getEmailInscrito());
            addLabel(sheet, 1, i, inscricao.getEvento().getNome());
            addLabel(sheet, 2, i, inscricao.getNomeInscrito());
            addLabel(sheet, 3, i, inscricao.getTelefoneInscrito());
            i++;
        }
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        sheet.addCell(new Label(column, row, s, FONTE_TEXTO));
    }
}