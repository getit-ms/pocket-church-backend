/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app;

import br.gafs.calvinista.dto.ParametrosIgrejaDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.view.relatorio.RelatorioUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author Gabriel
 */
public class ReportBuilder {
    private Igreja igreja;
    private ParametrosIgrejaDTO parametros;
    private Map<String, Object> args = new HashMap<String, Object>();
    private List<Object> data = new ArrayList<Object>();
    private String template;

    public ReportBuilder(Igreja igreja, ParametrosIgrejaDTO parametros) {
        this.igreja = igreja;
        this.parametros = parametros;
    }
    
    public static ReportBuilder init(Igreja igreja, ParametrosIgrejaDTO parametros){
        return new ReportBuilder(igreja, parametros);
    }
    
    public ReportBuilder template(String resource){
        template = resource;
        return this;
    }
    
    public ReportBuilder arg(String key, Object value){
        args.put(key, value);
        return this;
    }
    
    public ReportBuilder value(Object value){
        data.add(value);
        return this;
    }
    
    public ReportBuilder values(List<?> values){
        data.addAll(values);
        return this;
    }
    
    public byte[] build() throws JRException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        build(baos);
        return baos.toByteArray();
    }
     
    public void build(OutputStream os) throws JRException{
        RelatorioUtil.exportAsPDF(print(), os);
    }
    
    private JasperPrint print() throws JRException{
        Map<String, Object> args = new HashMap<String, Object>();
        
        if (this.parametros.getIcon() != null){
            args.put("logo", new ByteArrayInputStream(this.parametros.getIcon()));
        }
        
        args.put("igreja", this.igreja.getNome());
        args.put("args", this.args);
        args.put("values", this.data);
        args.put("subreport", this.template);
        
        return JasperFillManager.fillReport(getClass().getClassLoader().getResourceAsStream("jasper/template.jasper"), args);
    }
}
