package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.DAOService;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.DisplayFormat;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioTodosInscritos implements ProcessamentoRelatorioCache.Relatorio {

    private final static WritableCellFormat FONTE_HEADER =
            new WritableCellFormat(new WritableFont(WritableFont.TIMES,
                    10, WritableFont.BOLD, false));
    private final static WritableCellFormat FONTE_TEXTO = new WritableCellFormat(
            new WritableFont(WritableFont.TIMES, 10));
    private final static WritableCellFormat FONTE_DATA = new WritableCellFormat(
            new WritableFont(WritableFont.TIMES, 10),
            new DateFormat("dd/MM/yyyy HH:mm"));

    private Igreja igreja;
    private TipoEvento tipo;

    public RelatorioTodosInscritos(Igreja igreja, TipoEvento tipo){
        this.igreja = igreja;
        this.tipo = tipo;
    }

    @Override
    public String getId() {
        return igreja.getId().toString();
    }

    @Override
    public String getTitulo() {
        return "Inscritos em " + tipo.name() + " em " + igreja.getNome();
    }

    @Override
    public String getFilename() {
        return "Inscritos em " + tipo.name() + " em " + igreja.getNome();
    }

    @Override
    public ReportUtil.Reporter generate(final DAOService daoService) {
        final List<InscricaoEvento> inscricoes = daoService.findWith(QueryAdmin.
                INSCRICOES_EVENTOS_ATIVOS.create(tipo, igreja.getChave()));

        return new ReportUtil.Reporter(){

            @Override
            public void export(String tipo, OutputStream os) {
                try {
                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale("pt", "BR"));

                    WritableWorkbook workbook = Workbook.createWorkbook(os, wbSettings);

                    WritableSheet excelSheet = workbook.createSheet("Inscritos " + RelatorioTodosInscritos.this.tipo.name(), 0);

                    createCabecalhos(excelSheet);
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
        int i = 1;
        for (InscricaoEvento inscricao : inscricoes) {
            addLabel(sheet, 0, i, inscricao.getEmailInscrito());
            addLabel(sheet, 1, i, inscricao.getEvento().getNome());
            addLabel(sheet, 2, i, inscricao.getNomeInscrito());
            addLabel(sheet, 3, i, inscricao.getTelefoneInscrito());
            addDate(sheet, 4, i, inscricao.getEvento().getDataHoraInicio());
            addDate(sheet, 5, i, inscricao.getEvento().getDataHoraTermino());
            addDate(sheet, 6, i, inscricao.getData());
            i++;
        }
    }

    private void createCabecalhos(WritableSheet sheet) throws WriteException {
        addCaption(sheet, 0, 0, "E-MAIL");
        addCaption(sheet, 1, 0, TipoEvento.EBD.equals(this.tipo) ? "CURSO" : "EVENTO");
        addCaption(sheet, 2, 0, "NOME");
        addCaption(sheet, 3, 0, "TELEFONE");
        addCaption(sheet, 4, 0, "INICIO");
        addCaption(sheet, 5, 0, "TERMINO");
        addCaption(sheet, 6, 0, "INSCRICAO");
    }


    private void addCaption(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, FONTE_HEADER);
        sheet.addCell(label);
    }

    private void addDate(WritableSheet sheet, int column, int row, Date data) throws WriteException, RowsExceededException {
        sheet.addCell(new DateTime(column, row, data, FONTE_DATA));
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, FONTE_TEXTO);
        sheet.addCell(label);
    }
}
