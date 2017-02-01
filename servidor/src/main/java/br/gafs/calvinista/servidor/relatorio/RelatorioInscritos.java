package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.FiltroInscricao;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import br.gafs.view.relatorio.BuscaPaginadaDataSource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioInscritos implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private Long membro;
    private Evento evento;

    public RelatorioInscritos(Evento evento, Long membro){
        this.igreja = evento.getIgreja();
        this.evento = evento;
        this.membro = membro;
    }

    @Override
    public String getId() {
        return evento.getId().toString();
    }

    @Override
    public String getTitulo() {
        return evento.getNome();
    }

    @Override
    public ReportUtil.Exporter generate(final DAOService daoService) {
        return ReportUtil.igreja(
                "report/inscritos_evento.jasper",
                evento.getNome(),
                evento.getIgreja())
                .arg("EVENTO", evento)
                .dataSource(new BuscaPaginadaDataSource<>(new BuscaPaginadaDataSource.PaginaResolver<InscricaoEvento>() {
                    @Override
                    public BuscaPaginadaDTO<InscricaoEvento> buscaPagina(int pagina) {
                        return daoService.findWith(new FiltroInscricao(evento.getId(), igreja.getChave(), membro, new FiltroInscricaoDTO(pagina, 30)));
                    }
                })).build();
    }
}
