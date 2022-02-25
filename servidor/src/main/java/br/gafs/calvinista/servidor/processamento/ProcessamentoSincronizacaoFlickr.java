package br.gafs.calvinista.servidor.processamento;

import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.GaleriaFotos;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.servidor.flickr.FlickrService;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import lombok.RequiredArgsConstructor;

import javax.naming.InitialContext;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ProcessamentoSincronizacaoFlickr implements ProcessamentoService.Processamento {
    public static final String FLICKR_SERVICE_LOOKUP = "java:global/calvinista-app/calvinista-servidor/FlickrService";

    private final Igreja igreja;
    private final Date dataSincronizacao = new Date();
    private Integer pagina = 1;

    @Override
    public String getId() {
        return igreja.getChave();
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        final FlickrService flickrService = InitialContext.doLookup(FLICKR_SERVICE_LOOKUP);

        boolean hasProxima = tool.transactional(new ProcessamentoService.ExecucaoTransacional<Boolean>() {
            @Override
            public Boolean execute(DAOService daoService) {
                BuscaPaginadaDTO<GaleriaFotos> galerias = flickrService.buscaGaleriaFotos(igreja.getChave(), pagina);

                for (GaleriaFotos galeria : galerias) {
                    galeria.setSincronizacao(dataSincronizacao);
                    galeria.setIgreja(igreja);

                    daoService.update(galeria);
                }

                return galerias.isHasProxima();
            }
        });

        if (hasProxima) {
            pagina++;
            return 1;
        }

        return 0;
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {

        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                List<GaleriaFotos> galerias = daoService.findWith(QueryAdmin.GALERIA_FOTOS_ANTIGOS
                        .create(igreja.getChave(), dataSincronizacao));

                for (GaleriaFotos galeria : galerias) {
                    daoService.delete(GaleriaFotos.class, galeria.getId());
                }

                return null;
            }
        });
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {

    }
}
