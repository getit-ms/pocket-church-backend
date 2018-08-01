package br.gafs.pocket.corporate.servidor.processamento;

import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.ArquivoPDF;
import br.gafs.pocket.corporate.entity.BoletimInformativo;
import br.gafs.pocket.corporate.entity.RegistroEmpresaId;
import br.gafs.pocket.corporate.entity.domain.StatusBoletimInformativo;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.util.PDFToImageConverterUtil;
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
@EqualsAndHashCode(of = "bid")
public class ProcessamentoBoletim implements ProcessamentoService.Processamento {

    private static final Logger LOGGER = Logger.getLogger(ProcessamentoBoletim.class.getName());

    private BoletimInformativo boletimInformativo;
    private RegistroEmpresaId bid;

    public ProcessamentoBoletim(BoletimInformativo boletimInformativo) {
        this.boletimInformativo = boletimInformativo;
        this.bid = new RegistroEmpresaId(boletimInformativo.getChaveEmpresa(), boletimInformativo.getId());
    }

    @Override
    public String getId() {
        return bid.getChaveEmpresa() + "#" + bid.getId();
    }

    @Override
    public int step(final ProcessamentoService.ProcessamentoTool tool) throws Exception {
        LOGGER.info("Realizando passo de processamento de boletim " + boletimInformativo.getId() + ".");
        int total = trataPaginasPDF(new TransactionHandler() {
            @Override
            public <T> T transactional(ProcessamentoService.ExecucaoTransacional<T> execucaoTransacional) {
                return tool.transactional(execucaoTransacional);
            }
        }, boletimInformativo, 5);
        int current = boletimInformativo.getPaginas().size();

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<BoletimInformativo>() {
            @Override
            public BoletimInformativo execute(DAOService daoService) {
                return daoService.update(boletimInformativo);
            }
        });

        BoletimInformativo.lock(bid, (100 * current) / total);
        LOGGER.info("Passo de processamento de boletim " + boletimInformativo.getId() + " concluído. Andamento do processamento: " + current + " de " + total + ".");
        return tool.getStep() + ((int) Math.ceil((total - current) / 5d));
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        BoletimInformativo.unlock(bid);
        LOGGER.info("Finalizando processamento de boletim " + boletimInformativo.getId() + ".");

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                daoService.execute(QueryAdmin.UPDATE_STATUS_BOLETIM.
                        create(boletimInformativo.getChaveEmpresa(), boletimInformativo.getId(), StatusBoletimInformativo.PUBLICADO));
                return null;
            }
        });
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {
        BoletimInformativo.unlock(bid);
        LOGGER.severe("Abandonando processamento de boletim " + boletimInformativo.getId() + ".");

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                daoService.execute(QueryAdmin.UPDATE_STATUS_BOLETIM.
                        create(boletimInformativo.getChaveEmpresa(), boletimInformativo.getId(), StatusBoletimInformativo.REJEITADO));

                return null;
            }
        });
    }

    public static int trataPaginasPDF(final TransactionHandler tool, final ArquivoPDF pdf, final int limitePaginas) throws IOException {
        final int offset = pdf.getPaginas().size();

        return PDFToImageConverterUtil.convert(EntityFileManager.
                get(pdf.getPDF(), "dados"), offset, limitePaginas).forEachPage(new PDFToImageConverterUtil.PageHandler() {
            @Override
            public void handle(int page, byte[] dados) throws IOException {
                if (page < offset || (limitePaginas > 0 && page >= (offset + limitePaginas))){
                    return;
                }

                if (page == 0){
                    final Arquivo arquivo = new Arquivo(pdf.getEmpresa(), pdf.getPDF().getNome().
                            replaceFirst(".[pP][dD][fF]$", "") + "_thumbnail.png", dados);
                    arquivo.used();

                    Arquivo salvo = tool.transactional(new ProcessamentoService.ExecucaoTransacional<Arquivo>() {
                        @Override
                        public Arquivo execute(DAOService daoService) {
                            return daoService.update(arquivo);
                        }
                    });
                    pdf.setThumbnail(salvo);
                    salvo.clearDados();
                }

                final Arquivo pagina = new Arquivo(pdf.getEmpresa(), pdf.getPDF().getNome().
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

    public interface TransactionHandler {
        <T> T transactional(ProcessamentoService.ExecucaoTransacional<T> execucaoTransacional);
    }
}
