package br.gafs.calvinista.servidor.processamento;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.ArquivoPDF;
import br.gafs.calvinista.entity.Boletim;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.domain.StatusBoletim;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.util.PDFToImageConverterUtil;
import br.gafs.dao.DAOService;
import br.gafs.file.EntityFileManager;
import br.gafs.util.image.ImageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by mirante0 on 01/02/2017.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "bid")
public class ProcessamentoBoletim implements ProcessamentoService.Processamento {

    private Boletim boletim;
    private RegistroIgrejaId bid;

    public ProcessamentoBoletim(Boletim boletim) {
        this.boletim = boletim;
        this.bid = new RegistroIgrejaId(boletim.getChaveIgreja(), boletim.getId());
    }

    @Override
    public String getId() {
        return bid.getChaveIgreja() + "#" + bid.getId();
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        int total = trataPaginasPDF(tool.getDaoService(), boletim, 5);
        int current = boletim.getPaginas().size();
        tool.getDaoService().update(boletim);
        Boletim.lock(bid, (100 * current) / total);
        return tool.getStep() + ((int) Math.ceil((total - current) / 5d));
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        Boletim.unlock(bid);
        tool.getDaoService().execute(QueryAdmin.UPDATE_STATUS_BOLETIM.
                create(boletim.getChaveIgreja(), boletim.getId(), StatusBoletim.PUBLICADO));
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {
        Boletim.unlock(bid);
        tool.getDaoService().execute(QueryAdmin.UPDATE_STATUS_BOLETIM.
                create(boletim.getChaveIgreja(), boletim.getId(), StatusBoletim.REJEITADO));
    }

    public static int trataPaginasPDF(final DAOService daoService, final ArquivoPDF pdf, final int limitePaginas) throws IOException {
        final int offset = pdf.getPaginas().size();

        return PDFToImageConverterUtil.convert(EntityFileManager.
                get(pdf.getPDF(), "dados")).forEachPage(new PDFToImageConverterUtil.PageHandler() {
            @Override
            public void handle(int page, byte[] dados) throws IOException {
                if (page < offset || (limitePaginas > 0 && page >= (offset + limitePaginas))){
                    return;
                }

                if (page == 0){
                    Arquivo arquivo = new Arquivo(pdf.getIgreja(), pdf.getPDF().getNome().
                            replaceFirst(".[pP][dD][fF]$", "") + "_thumbnail.png", ImageUtil.redimensionaImagem(dados, 500, 500));
                    arquivo.used();
                    arquivo = daoService.update(arquivo);
                    pdf.setThumbnail(arquivo);
                    arquivo.clearDados();
                }

                Arquivo pagina = new Arquivo(pdf.getIgreja(), pdf.getPDF().getNome().
                        replaceFirst(".[pP][dD][fF]$", "") + "_page"
                        + new DecimalFormat("00000").format(page + 1) + ".png", dados);;
                pagina.used();
                pagina = daoService.update(pagina);
                pdf.getPaginas().add(pagina);
                pagina.clearDados();
            }
        });
    }
}
