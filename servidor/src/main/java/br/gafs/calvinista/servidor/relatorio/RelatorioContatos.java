package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.FiltroMembro;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dto.FiltroMembroDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.util.date.DateUtil;
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
public class RelatorioContatos implements ProcessamentoRelatorioCache.Relatorio {

    private final static WritableCellFormat FONTE_TEXTO = new WritableCellFormat(
            new WritableFont(WritableFont.TIMES, 10));

    private Igreja igreja;

    public RelatorioContatos(Igreja igreja){
        this.igreja = igreja;
    }

    @Override
    public String getId() {
        return igreja.getId().toString();
    }

    @Override
    public String getTitulo() {
        return "Contatos de " + igreja.getNome();
    }

    @Override
    public String getFilename() {
        return "Contatos de " + igreja.getNome();
    }

    @Override
    public ReportUtil.Reporter generate(final ProcessamentoService.ProcessamentoTool tool) {
        return new ReportUtil.Reporter(){

            @Override
            public void export(String tipo, OutputStream os) {
                try {
                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale("pt", "BR"));

                    WritableWorkbook workbook = Workbook.createWorkbook(os, wbSettings);

                    WritableSheet excelSheet = workbook.createSheet("Contatos ", 0);

                    createLinhas(excelSheet, tool);

                    workbook.write();
                    workbook.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    private void createLinhas(WritableSheet sheet, ProcessamentoService.ProcessamentoTool tool) throws WriteException {
        final FiltroMembroDTO filtro = new FiltroMembroDTO();

        int i = 0;
        BuscaPaginadaDTO<Membro> resultado;
        do {
            resultado = tool.transactional(new ProcessamentoService.ExecucaoTransacional<BuscaPaginadaDTO<Membro>>() {
                @Override
                public BuscaPaginadaDTO<Membro> execute(DAOService daoService) {
                    return daoService.findWith(new FiltroMembro(true, igreja.getChave(), filtro));
                }
            });

            for (Membro membro : resultado.getResultados()) {
                addLabel(sheet, 0, i, membro.getNome());

                if (membro.getEmail() != null) {
                    addLabel(sheet, 1, i, membro.getEmail());
                }

                if (membro.getDataNascimento() != null) {
                    addLabel(sheet, 2, i, DateUtil.formataData(membro.getDataNascimento(), "dd/MM/yyyy"));
                }

                if (membro.getTelefones() != null) {
                    addLabel(sheet, 3, i, concat(membro.getTelefones()));
                }

                if (membro.getEndereco() != null) {
                    addLabel(sheet, 4, i, membro.getEndereco().getDescricao());
                    addLabel(sheet, 5, i, membro.getEndereco().getCidade());
                    addLabel(sheet, 6, i, membro.getEndereco().getEstado());
                    addLabel(sheet, 7, i, membro.getEndereco().getCep());
                }

                i++;
            }

            filtro.setPagina(filtro.getPagina() + 1);
        }while(resultado.isHasProxima());

    }

    private String concat(List<String> telefones) {
        StringBuilder tels = new StringBuilder();

        for (String telefone : telefones) {
            if (tels.length() > 0) {
                tels.append(", ");
            }
            tels.append(telefone);
        }

        return tels.toString();
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        sheet.addCell(new Label(column, row, s, FONTE_TEXTO));
    }
}
