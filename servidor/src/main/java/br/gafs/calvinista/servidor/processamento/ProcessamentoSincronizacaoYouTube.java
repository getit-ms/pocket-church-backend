package br.gafs.calvinista.servidor.processamento;

import br.gafs.dao.DAOService;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Video;
import br.gafs.calvinista.entity.VideoId;
import br.gafs.calvinista.servidor.ProcessamentoService;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ProcessamentoSincronizacaoYouTube implements ProcessamentoService.Processamento {
    private final Igreja igreja;
    private final List<Video> videos;
    private final Date dataSincronizacao = new Date();

    @Override
    public String getId() {
        return igreja.getChave();
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        int i = 0;

        while (i < 10 && !videos.isEmpty()) {
            final Video video = videos.remove(0);

            tool.transactional(new ProcessamentoService.ExecucaoTransacional<Video>() {
                @Override
                public Video execute(DAOService daoService) {
                    video.setIgreja(igreja);
                    video.setDataAtualizacao(dataSincronizacao);

                    return daoService.update(video);
                }
            });

            i++;
        }

        return videos.size();
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        tool.transactional(new ProcessamentoService.ExecucaoTransacional<Object>() {
            @Override
            public Object execute(DAOService daoService) {
                List<Video> videos = daoService.findWith(QueryAdmin.VIDEOS_ANTIGOS.create(igreja.getChave(), dataSincronizacao));

                for (Video video : videos) {
                    daoService.delete(Video.class, new VideoId(video.getId(), video.getChaveIgreja()));
                }

                return null;
            }
        });
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {

    }
}
