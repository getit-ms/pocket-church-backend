package br.gafs.calvinista.util;

import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Template;
import br.gafs.exceptions.ServiceException;
import br.gafs.file.EntityFileManager;
import br.gafs.view.relatorio.RelatorioUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;

import java.io.OutputStream;
import java.util.*;

/**
 * Created by mirante0 on 20/01/2017.
 */
public class ReportUtil {
    private final String path;
    private final Map<String, Object> args = new HashMap<String, Object>();
    private JRDataSource ds;

    private ReportUtil(String path) {
        this.path = path;
    }

    public static ReportUtil basic(String path) {
        return new ReportUtil(path);
    }

    public static ReportUtil igreja(String path,
                                    final String titulo,
                                    final Igreja igreja,
                                    final Template template) {
        return new ReportUtil(path) {
            @Override
            public ExporterImpl build() {
                return basic("report/relatorio_igreja.jasper")
                        .arg("LOGO_IGREJA", template.getLogoReports() == null ? null :
                                EntityFileManager.get(template.getLogoReports(), "dados").getAbsolutePath())
                        .arg("TITULO", titulo)
                        .arg("REPORT_LOCALE", new Locale(igreja.getLocale()))
                        .arg("REPORT_TIME_ZONE", TimeZone.getTimeZone(igreja.getTimezone()))
                        .bean(this).build();
            }
        };
    }

    public ReportUtil arg(String key, Object value) {
        this.args.put(key, value);
        return this;
    }

    public ReportUtil args(Map<String, Object> args) {
        this.args.putAll(args);
        return this;
    }

    public ReportUtil dataSource(JRDataSource ds) {
        this.ds = ds;
        return this;
    }

    public ReportUtil collection(Collection<?> col) {
        return dataSource(new JRBeanCollectionDataSource(col));
    }

    public ReportUtil bean(Object obj) {
        return collection(Arrays.asList(new Object[]{obj}));
    }

    public ExporterImpl build() {
        try {
            return new ExporterImpl(JasperFillManager.fillReport(ReportUtil.
                    class.getClassLoader().getResourceAsStream(path), args, ds));
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public String getReportPath() {
        return path;
    }

    public Map<String, Object> getArguments() {
        return args;
    }

    public JRDataSource getDataSource() {
        return ds;
    }

    public interface Reporter {
        void export(String tipo, OutputStream os) throws JRException;
    }

    public class ExporterImpl implements Reporter {
        private JasperPrint print;

        public ExporterImpl(JasperPrint print) {
            this.print = print;
        }

        public void pdf(OutputStream os) throws JRException {
            RelatorioUtil.exportAsPDF(print, os);
        }

        public void docx(OutputStream os) throws JRException {
            JRDocxExporter exporter = new JRDocxExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
        }

        public void xls(OutputStream os) throws JRException {
            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
            exporter.exportReport();
        }

        public void export(String tipo, OutputStream os) throws JRException {
            if (tipo.matches("pdf|docx|xls")) {
                switch (tipo) {
                    case "pdf":
                        pdf(os);
                    case "docx":
                        docx(os);
                    case "xls":
                        xls(os);
                }
                return;
            }

            throw new ServiceException("Invalid Format");
        }
    }
}
