package br.gafs.calvinista.app.util;

import br.gafs.view.relatorio.RelatorioUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mirante0 on 20/01/2017.
 */
public class ReportUtil {
    private final String path;
    private final Map<String, Object> args = new HashMap<String, Object>();
    private JRDataSource ds;

    private ReportUtil(String path){
        this.path = path;
    }

    public static ReportUtil basic(String path){
        return new ReportUtil(path);
    }

    public static ReportUtil igreja(String path,
                                    final String titulo,
                                    final String igreja,
                                    final ServletContext sctxt){
        return new ReportUtil(sctxt.getRealPath(path)){
            @Override
            public Exporter build() {
                return basic(sctxt.getRealPath("/WEB-INF/jasper/relatorio_igreja.jasper"))
                        .arg("LOGO_IGREJA", igreja)
                        .arg("TITULO", titulo)
                        .bean(this).build();
            }
        };
    }

    public ReportUtil arg(String key, Object value){
        args.put(key, value);
        return this;
    }

    public ReportUtil args(Map<String, Object> args){
        args.putAll(args);
        return this;
    }

    public ReportUtil dataSource(JRDataSource ds){
        this.ds = ds;
        return this;
    }

    public ReportUtil collection(Collection<?> col){
        return dataSource(new JRBeanCollectionDataSource(col));
    }

    public ReportUtil bean(Object obj){
        return dataSource(new JRBeanCollectionDataSource(Arrays.asList(new Object[]{obj})));
    }

    public Exporter build(){
        return new Exporter(RelatorioUtil.gerarRelatorio(path, args, ds));
    }

    public String getReportPath(){
        return path;
    }

    public Map<String, Object> getArguments(){
        return args;
    }

    public JRDataSource getDataSource(){
        return ds;
    }

    public class Exporter {
        private JasperPrint print;

        public Exporter(JasperPrint print){
            this.print = print;
        }

        public byte[] pdf() throws JRException {
            return RelatorioUtil.exportAsPDF(print);
        }
    }
}
