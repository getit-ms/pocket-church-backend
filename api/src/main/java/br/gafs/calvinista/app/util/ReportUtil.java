package br.gafs.calvinista.app.util;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.util.ResourceUtil;
import br.gafs.exceptions.ServiceException;
import br.gafs.view.relatorio.RelatorioUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.servlet.ServletContext;
import java.util.*;

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
                                    final Igreja igreja,
                                    final ServletContext sctxt){
        return new ReportUtil(sctxt.getRealPath(path)){
            @Override
            public Exporter build() {
                return basic(sctxt.getRealPath("/WEB-INF/report/relatorio_igreja.jasper"))
                        .arg("LOGO_IGREJA", ResourceUtil.report(igreja.getChave(), "logo.png"))
                        .arg("TITULO", titulo)
                        .arg("REPORT_LOCALE", new Locale(igreja.getLocale()))
                        .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(igreja.getTimezone()))
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

        public byte[] docx() throws JRException {
            return RelatorioUtil.exportAsDocx(print);
        }

        public byte[] xls() throws JRException {
            return RelatorioUtil.exportAsXLS(print);
        }

        public byte[] export(String tipo) throws JRException {
            if (tipo.matches("pdf|docx|xls")){
                switch (tipo){
                    case "pdf":
                        return pdf();
                    case "docx":
                        return docx();
                    case "xls":
                        return xls();
                }
            }

            throw new ServiceException("Invalid Format");
        }
    }
}
