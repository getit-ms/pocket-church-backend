/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.util;

import br.gafs.calvinista.dto.CalvinEmailDTO;
import br.gafs.calvinista.dto.CalvinEmailDTO.Materia;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Institucional;

import java.io.FileInputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
            content.append(loadManchete(institucional, email.getManchete(), attachments, attachNames));
        }
        
        for (Materia materia : email.getMaterias()){
            if (materia instanceof CalvinEmailDTO.MateriaIlustrada){
                content.append(loadMateriaIlustrada(institucional, (CalvinEmailDTO.MateriaIlustrada) materia, attachments, attachNames));
            }else if (materia instanceof CalvinEmailDTO.MateriaDupla){
                content.append(loadMateriaDupla(institucional, (CalvinEmailDTO.MateriaDupla) materia, attachments, attachNames));
            }else{
                content.append(loadMateria(institucional, materia, attachments, attachNames));
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

    private static String loadManchete(Institucional institucional, CalvinEmailDTO.Manchete manchete, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo", manchete.getTitulo());
        args.put("texto", manchete.getTexto());
        args.put("link.url", manchete.getLinkURL());
        args.put("link.texto", manchete.getLinkTexto());
        args.put("img_fundo", "cid:attachment" + attachments.size());
        attachments.add(new MensagemEmailDTO.Anexo(ResourceUtil.mensagem(institucional.getIgreja().getChave(), "/email/img_fundo.png"), MensagemEmailDTO.TipoAnexo.ARQUIVO));
        attachNames.add("img_fundo.png");
        return load(institucional.getIgreja(), "/email/titulo-manchete.html", args);
    }
    
    private final static Pattern dataPattern = Pattern.compile("\\{\\{[^\\}]+\\}\\}");
    
    private static String load(Igreja igreja, String resource, Map<String, String> args){
        try{
            Scanner scn = new Scanner(new FileInputStream(ResourceUtil.mensagem(igreja.getChave(), resource)), "UTF-8");

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
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static String loadMateriaIlustrada(Institucional institucional, CalvinEmailDTO.MateriaIlustrada materia, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo", materia.getTitulo());
        args.put("texto", materia.getTexto());
        args.put("link.url", materia.getLinkURL());
        args.put("link.texto", materia.getLinkTexto());
        args.put("imagem", "cid:attachment" + attachments.size());
        attachments.add(new MensagemEmailDTO.Anexo(materia.getImagem().getAbsolutePath(), MensagemEmailDTO.TipoAnexo.ARQUIVO));
        attachNames.add("imagem" + attachments.size() + ".png");
        return load(institucional.getIgreja(), "/email/materia-ilustrada-link.html", args);
    }

    private static String loadMateriaDupla(Institucional institucional, CalvinEmailDTO.MateriaDupla materia, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo1", materia.getTitulo());
        args.put("texto1", materia.getTexto());
        args.put("titulo2", materia.getTitulo2());
        args.put("texto2", materia.getTexto2());
        return load(institucional.getIgreja(), "/email/materia-double.html", args);
    }

    private static String loadMateria(Institucional institucional, CalvinEmailDTO.Materia materia, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("titulo", materia.getTitulo());
        args.put("texto", materia.getTexto());
        return load(institucional.getIgreja(), "/email/materia-single.html", args);
    }

    private static String loadEmail(Institucional institucional, String content, List<MensagemEmailDTO.Anexo> attachments, List<String> attachNames) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("content", content);
        args.put("igreja.nome", institucional.getIgreja().getNome());
        args.put("igreja.site", institucional.getSite());
        args.put("logo", "cid:attachment" + attachments.size());
        attachments.add(new MensagemEmailDTO.Anexo(ResourceUtil.mensagem(institucional.getIgreja().getChave(), "/email/logo.png"), MensagemEmailDTO.TipoAnexo.ARQUIVO));
        attachNames.add("logo.png");
        return load(institucional.getIgreja(), "/email.html", args);
    }
}
