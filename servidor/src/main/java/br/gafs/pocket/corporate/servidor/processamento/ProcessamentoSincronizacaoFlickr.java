package br.gafs.pocket.corporate.servidor.processamento;

import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.GaleriaFotos;
import br.gafs.pocket.corporate.servidor.ProcessamentoService;
import br.gafs.pocket.corporate.servidor.flickr.FlickrService;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ProcessamentoSincronizacaoFlickr implements ProcessamentoService.Processamento {
    private final Empresa empresa;
    private final Date dataSincronizacao = new Date();
    private Integer pagina = 1;

    @Override
    public String getId() {
        return empresa.getChave();
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        final FlickrService flickrService = tool.getSessionContext().getBusinessObject(FlickrService.class);

        boolean hasProxima = tool.transactional(new ProcessamentoService.ExecucaoTransacional<Boolean>() {
            @Override
            public Boolean execute(DAOService daoService) {
                BuscaPaginadaDTO<GaleriaFotos> galerias = flickrService.buscaGaleriaFotos(empresa.getChave(), pagina);

                for (GaleriaFotos galeria : galerias) {
                    galeria.setSincronizacao(dataSincronizacao);
                    galeria.setEmpresa(empresa);

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
                                .create(empresa.getChave(), dataSincronizacao));

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
