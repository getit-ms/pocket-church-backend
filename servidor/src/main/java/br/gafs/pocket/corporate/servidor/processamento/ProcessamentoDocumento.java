package br.gafs.pocket.corporate.servidor.processamento;

import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.pocket.corporate.entity.ArquivoPDF;
import br.gafs.pocket.corporate.entity.Documento;
import br.gafs.pocket.corporate.entity.RegistroEmpresaId;
import br.gafs.pocket.corporate.entity.domain.StatusDocumento;
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
public class ProcessamentoDocumento implements ProcessamentoService.Processamento {

    private static final Logger LOGGER = Logger.getLogger(ProcessamentoDocumento.class.getName());

    private Documento documento;
    private RegistroEmpresaId bid;

    public ProcessamentoDocumento(Documento documento) {
        this.documento = documento;
        this.bid = new RegistroEmpresaId(documento.getChaveEmpresa(), documento.getId());
    }

    @Override
    public String getId() {
        return bid.getChaveEmpresa() + "#" + bid.getId();
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        LOGGER.info("Realizando passo de processamento de documento " + documento.getId() + ".");
        int total = trataPaginasPDF(tool, documento, 5);
        int current = documento.getPaginas().size();
        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Documento>() {
            @Override
            public Documento execute(DAOService daoService) {
                return daoService.update(documento);
            }
        });
        Documento.lock(bid, (100 * current) / total);
        LOGGER.info("Passo de processamento de documento " + documento.getId() + " concluído. Andamento do processamento: " + current + " de " + total + ".");
        return tool.getStep() + ((int) Math.ceil((total - current) / 5d));
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        Documento.unlock(bid);
        LOGGER.info("Finalizando processamento de documento " + documento.getId() + ".");

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                daoService.execute(QueryAdmin.UPDATE_STATUS_DOCUMENTO.
                        create(documento.getChaveEmpresa(), documento.getId(), StatusDocumento.PUBLICADO));
                return null;
            }
        });
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {
        Documento.unlock(bid);
        LOGGER.severe("Abandonando processamento de documento " + documento.getId() + ".");

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                daoService.execute(QueryAdmin.UPDATE_STATUS_DOCUMENTO.
                        create(documento.getChaveEmpresa(), documento.getId(), StatusDocumento.REJEITADO));
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
                    final Arquivo arquivo = new Arquivo(pdf.getEmpresa(), pdf.getPDF().getNome().
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
}
