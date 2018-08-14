package br.gafs.calvinista.servidor.processamento;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Arquivo;
import br.gafs.calvinista.entity.ArquivoPDF;
import br.gafs.calvinista.entity.Estudo;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.domain.StatusEstudo;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.util.PDFToImageConverterUtil;
import br.gafs.dao.DAOService;
import br.gafs.file.EntityFileManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Logger;

/**
 * Created by mirante0 on 01/02/2017.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "eid")
public class ProcessamentoEstudo implements ProcessamentoService.Processamento {

    private static final Logger LOGGER = Logger.getLogger(ProcessamentoEstudo.class.getName());

    private Estudo estudo;
    private RegistroIgrejaId bid;

    public ProcessamentoEstudo(Estudo estudo) {
        this.estudo = estudo;
        this.bid = new RegistroIgrejaId(estudo.getChaveIgreja(), estudo.getId());
    }

    @Override
    public String getId() {
        return bid.getChaveIgreja() + "#" + bid.getId();
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        LOGGER.info("Realizando passo de processamento de estudo " + estudo.getId() + ".");
        int total = trataPaginasPDF(tool, estudo, 5);
        int current = estudo.getPaginas().size();
        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Estudo>() {
            @Override
            public Estudo execute(DAOService daoService) {
                return daoService.update(estudo);
            }
        });
        Estudo.lock(bid, (100 * current) / total);
        LOGGER.info("Passo de processamento de estudo " + estudo.getId() + " concluído. Andamento do processamento: " + current + " de " + total + ".");
        return tool.getStep() + ((int) Math.ceil((total - current) / 5d));
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        Estudo.unlock(bid);
        LOGGER.info("Finalizando processamento de estudo " + estudo.getId() + ".");

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                daoService.execute(QueryAdmin.UPDATE_STATUS_ESTUDO.
                        create(estudo.getChaveIgreja(), estudo.getId(), StatusEstudo.PUBLICADO));
                return null;
            }
        });
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {
        Estudo.unlock(bid);
        LOGGER.severe("Abandonando processamento de estudo " + estudo.getId() + ".");

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                daoService.execute(QueryAdmin.UPDATE_STATUS_ESTUDO.
                        create(estudo.getChaveIgreja(), estudo.getId(), StatusEstudo.REJEITADO));
                return null;
            }
        });
    }

    public static int trataPaginasPDF(final ProcessamentoService.ProcessamentoTool tool, final ArquivoPDF pdf, final int limitePaginas) throws IOException {
        final int offset = pdf.getPaginas().size();

        return PDFToImageConverterUtil.convert(EntityFileManager.
                get(pdf.getPDF(), "dados"), offset, limitePaginas).forEachPage(new PDFToImageConverterUtil.PageHandler() {
            @Override
            public void handle(int page, byte[] dados) throws IOException {
                if (page < offset || (limitePaginas > 0 && page >= (offset + limitePaginas))){
                    return;
                }

                if (page == 0){
                    final Arquivo arquivo = new Arquivo(pdf.getIgreja(), pdf.getPDF().getNome().
                            replaceFirst(".[pP][dD][fF]$", "") + "_thumbnail.png", dados);
                    arquivo.used();

                    Arquivo arquivoSalvo = tool.transactional(new ProcessamentoService.ExecucaoTransacional<Arquivo>() {
                        @Override
                        public Arquivo execute(DAOService daoService) {
                            return daoService.update(arquivo);
                        }
                    });
                    pdf.setThumbnail(arquivoSalvo);
                    arquivoSalvo.clearDados();
                }

                final Arquivo pagina = new Arquivo(pdf.getIgreja(), pdf.getPDF().getNome().
                        replaceFirst(".[pP][dD][fF]$", "") + "_page"
                        + new DecimalFormat("00000").format(page + 1) + ".png", dados);;
                pagina.used();

                Arquivo paginaSalva = tool.transactional(new ProcessamentoService.ExecucaoTransacional<Arquivo>() {
                    @Override
                    public Arquivo execute(DAOService daoService) {
                        return daoService.update(pagina);
                    }
                });
                pdf.getPaginas().add(paginaSalva);
                paginaSalva.clearDados();
            }
        });
    }
}
