/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.util;

import br.gafs.calvinista.dto.CalvinEmailDTO;
import br.gafs.calvinista.dto.CalvinEmailDTO.Materia;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.entity.Institucional;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Gabriel
 */
public class MensagemUtil {
    public static MensagemEmailDTO email(Institucional institucional, String subject, CalvinEmailDTO email){
        List<MensagemEmailDTO.Anexo> attachments = new ArrayList<MensagemEmailDTO.Anexo>();
        List<String> attachNames = new ArrayList<String>();
        StringBuilder content = new StringBuilder();
        
        if (email.getManchete() != null){
            content.append(loadManchete(email.getManchete(), attachments, attachNames));
        }
        
        for (Materia materia : email.getMaterias()){
            if (materia instanceof CalvinEmailDTO.MateriaIlustrada){
                content.append(loadMateriaIlustrada((CalvinEmailDTO.MateriaIlustrada) materia, attachments, attachNames));
            }else if (materia instanceof CalvinEmailDTO.MateriaDupla){
                content.append(loadMateriaDupla((CalvinEmailDTO.MateriaDupla) materia, attachments, attachNames));
            }else{
                content.append(loadMateria(materia, attachments, attachNames));
            }
        }
        
        return new MensagemEmailDTO(subject, loadEmail(institucional, content.toString(), attachments, attachNames), 
                attachments.toArray(new MensagemEmailDTO.Anexo[0]), attachNames.toArray(new String[0]));
    }
    
    public static String getMensagem(String key, String locale, Object... args){
        ResourceBundle bundle = ResourceBundle.getBundle("mensagens", new Locale(locale));
        return MessageFormat.format(bundle.getString(key), args);
    }
    public static String formataHora(Date data, String locale, String timezone){
        return format(data, getMensagem("format.hora", locale), timezone);
    }
    public static String formataDataHora(Date data, String locale, String timezone){
        return format(data, getMensagem("format.data_hora", locale), timezone);
    }
    public static String formataData(Date data, String locale, String timezone){
        return format(data, getMensagem("format.data", locale), timezone);
    }
    
    public static String format(Date data, String pattern, String timezone){
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone(timezone));
        return formatter.format(data);
    }

    private static String loadManchete(CalvinEmailDTO.Manchete manchete, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo", manchete.getTitulo());
        args.put("texto", manchete.getTexto());
        args.put("link.url", manchete.getLinkURL());
        args.put("link.texto", manchete.getLinkTexto());
        args.put("img_fundo", "cid:attachment" + attachments.size());
        attachments.add(new MensagemEmailDTO.Anexo(MensagemUtil.class.getResource("/mensagem/email/img_fundo.png").toString(), MensagemEmailDTO.TipoAnexo.URL));
        attachNames.add("img_fundo.png");
        return load("/mensagem/email/titulo-manchete.html", args);
    }
    
    private final static Pattern dataPattern = Pattern.compile("\\{\\{[^\\}]+\\}\\}");
    
    private static String load(String resource, Map<String, String> args){
        Scanner scn = new Scanner(MensagemUtil.class.getResourceAsStream(resource), "UTF-8");
        
        StringBuilder str = new StringBuilder();
        while (scn.hasNext()){
            str.append(scn.nextLine().trim());
        }
        
        scn.close();
        
        Matcher matcher = dataPattern.matcher(str);
        
        while (matcher.find()){
            String value = args.get(matcher.group().replaceAll("[\\{\\}]", ""));
            if (value == null) value = "";
            str.replace(matcher.start(), matcher.end(), value);
            matcher = dataPattern.matcher(str);
        }
        
        return str.toString();
    }

    private static String loadMateriaIlustrada(CalvinEmailDTO.MateriaIlustrada materia, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo", materia.getTitulo());
        args.put("texto", materia.getTexto());
        args.put("link.url", materia.getLinkURL());
        args.put("link.texto", materia.getLinkTexto());
        args.put("imagem", "cid:attachment" + attachments.size());
        attachments.add(new MensagemEmailDTO.Anexo(materia.getImagem().getAbsolutePath(), MensagemEmailDTO.TipoAnexo.ARQUIVO));
        attachNames.add("imagem" + attachments.size() + ".png");
        return load("/mensagem/email/materia-ilustrada-link.html", args);
    }

    private static String loadMateriaDupla(CalvinEmailDTO.MateriaDupla materia, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo1", materia.getTitulo());
        args.put("texto1", materia.getTexto());
        args.put("titulo2", materia.getTitulo2());
        args.put("texto2", materia.getTexto2());
        return load("/mensagem/email/materia-double.html", args);
    }

    private static String loadMateria(CalvinEmailDTO.Materia materia, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo", materia.getTitulo());
        args.put("texto", materia.getTexto());
        return load("/mensagem/email/materia-single.html", args);
    }

    private static String loadEmail(Institucional institucional, String content, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("content", content);
        args.put("igreja.nome", institucional.getIgreja().getNome());
        args.put("igreja.site", institucional.getSite());
        args.put("logo", "cid:attachment" + attachments.size());
        attachments.add(new MensagemEmailDTO.Anexo(MensagemUtil.class.getResource("/mensagem/email/logo.png").toString(), MensagemEmailDTO.TipoAnexo.URL));
        attachNames.add("logo.png");
        return load("/mensagem/email.html", args);
    }
}
