package br.gafs.calvinista.servidor.processamento;

import br.gafs.bean.IEntity;
import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.servidor.ProcessamentoService;
import br.gafs.calvinista.util.Persister;
import br.gafs.calvinista.util.ReportUtil;
import br.gafs.dao.DAOService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mirante0 on 01/02/2017.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProcessamentoRelatorioCache implements ProcessamentoService.Processamento {
    private static final File dir = new File(ResourceBundleUtil._default().getPropriedade("REPORT_CACHE_DIR"));

    private String id;
    private String igreja;
    private Class<? extends Relatorio> relatorio;
    private String type;

    @Override
    public String getId() {
        return igreja + "#" + id + "." + type;
    }

    public ProcessamentoRelatorioCache(Relatorio relatorio, String type) throws IOException {
        this.id = relatorio.getId();
        this.igreja = relatorio.getIgreja().getChave();
        this.relatorio = relatorio.getClass();
        this.type = type;

        Persister.save(relatorio);
    }

    public static File file(Relatorio relatorio, String type){
        return new File(new File(new File(new File(dir, relatorio.getIgreja().getChave()),
                relatorio.getClass().getName().replaceAll("[\\.\\:]", "_")),
                relatorio.getId()), relatorio.getTitulo() + "." + type);
    }

    @Override
    public int step(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        Relatorio report = Persister.load(relatorio, getId());

        File file = file(report, type);

        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        report.generate(tool.getDaoService()).export(type, new FileOutputStream(file));

        return tool.getStep();
    }

    @Override
    public void finished(ProcessamentoService.ProcessamentoTool tool) throws Exception {
        Persister.remove(relatorio, getId());
    }

    @Override
    public void dropped(ProcessamentoService.ProcessamentoTool tool) {
        Persister.remove(relatorio, getId());
    }

    public interface Relatorio extends IEntity {
        String getId();
        Igreja getIgreja();
        String getTitulo();
        ReportUtil.Exporter generate(DAOService daoService);
    }

}