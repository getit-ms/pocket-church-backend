package br.gafs.calvinista.servidor.relatorio;

import br.gafs.calvinista.dao.FiltroInscricao;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dto.FiltroInscricaoDTO;
import br.gafs.calvinista.entity.Evento;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.InscricaoEvento;
import br.gafs.calvinista.entity.domain.TipoEvento;
import br.gafs.calvinista.servidor.processamento.ProcessamentoRelatorioCache;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.dao.DAOService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mirante0 on 01/02/2017.
 */

@Data
@NoArgsConstructor
public class RelatorioTodosInscritos implements ProcessamentoRelatorioCache.Relatorio {
    private Igreja igreja;
    private TipoEvento tipo;

    public RelatorioTodosInscritos(Igreja igreja, TipoEvento tipo){
        this.igreja = igreja;
        this.tipo = tipo;
    }

    @Override
    public String getId() {
        return igreja.getId().toString();
    }

    @Override
    public String getTitulo() {
        return "Inscritos em " + tipo.name() + " em " + igreja.getNome();
    }

    @Override
    public String getFilename() {
        return "inscritos_" + tipo.name().toLowerCase() + "_" + igreja.getChave();
    }

    @Override
    public ReportUtil.Exporter generate(final DAOService daoService) {
        return ReportUtil.basic(
                "report/inscritos_igreja.jasper").bean(daoService.findWith(QueryAdmin.
                INSCRICOES_EVENTOS_ATIVOS.create(igreja.getChave()))).arg("TIPO", tipo).build();
    }
}
