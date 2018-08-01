package br.gafs.pocket.corporate.servidor.relatorio;

import br.gafs.pocket.corporate.dao.FiltroColaborador;
import br.gafs.pocket.corporate.dto.FiltroColaboradorDTO;
import br.gafs.pocket.corporate.entity.Colaborador;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Colaborador;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.pocket.corporate.util.ReportUtil;
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

    private Empresa empresa;

    public RelatorioContatos(Empresa empresa){
        this.empresa = empresa;
    }

    @Override
    public String getId() {
        return empresa.getId().toString();
    }

    @Override
    public String getTitulo() {
        return "Contatos de " + empresa.getNome();
    }

    @Override
    public String getFilename() {
        return "Contatos de " + empresa.getNome();
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
        final FiltroColaboradorDTO filtro = new FiltroColaboradorDTO();

        int i = 0;
        BuscaPaginadaDTO<Colaborador> resultado;
        do {
            resultado = tool.transactional(new ProcessamentoService.ExecucaoTransacional<BuscaPaginadaDTO<Colaborador>>() {
                @Override
                public BuscaPaginadaDTO<Colaborador> execute(DAOService daoService) {
                    return daoService.findWith(new FiltroColaborador(true, empresa.getChave(), filtro));
                }
            });

            for (Colaborador colaborador : resultado.getResultados()) {
                addLabel(sheet, 0, i, colaborador.getNome());

                if (colaborador.getEmail() != null) {
                    addLabel(sheet, 1, i, colaborador.getEmail());
                }

                if (colaborador.getDataNascimento() != null) {
                    addLabel(sheet, 2, i, DateUtil.formataData(colaborador.getDataNascimento(), "dd/MM/yyyy"));
                }

                if (colaborador.getTelefones() != null) {
                    addLabel(sheet, 3, i, concat(colaborador.getTelefones()));
                }

                if (colaborador.getEndereco() != null) {
                    addLabel(sheet, 4, i, colaborador.getEndereco().getDescricao());
                    addLabel(sheet, 5, i, colaborador.getEndereco().getCidade());
                    addLabel(sheet, 6, i, colaborador.getEndereco().getEstado());
                    addLabel(sheet, 7, i, colaborador.getEndereco().getCep());
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
