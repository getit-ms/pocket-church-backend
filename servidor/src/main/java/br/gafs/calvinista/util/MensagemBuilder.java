/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.util;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.dto.MensagemEmailDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Template;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.file.EntityFileManager;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Gabriel
 */
@Singleton
public class MensagemBuilder {

    public static final String CID_ATTACHMENT_LOGO = "cid:attachment0";
    public static final String CID_ATTACHMENT_BANNER = "cid:attachment1";

    @EJB
    private CustomDAOService daoService;

    @EJB
    private ParametroService paramService;

    public MensagemEmailDTO email(Igreja igreja, TipoParametro subject, TipoParametro email, Object... args) {
        List<Object> allArgs = new ArrayList<Object>();

        allArgs.add(igreja.getChave());
        allArgs.add(igreja.getNome());
        allArgs.add(igreja.getNomeAplicativo());
        allArgs.add(CID_ATTACHMENT_LOGO);
        allArgs.add(CID_ATTACHMENT_BANNER);
        allArgs.addAll(Arrays.asList(args));

        String ssubject = paramService.get(igreja.getChave(), subject);
        String semail = MessageFormat.format(
                (String) paramService.get(igreja.getChave(), email),
                allArgs.toArray()
        );

        List<MensagemEmailDTO.Anexo> attachments = new ArrayList<MensagemEmailDTO.Anexo>();
        List<String> attachmentsNames = new ArrayList<String>();

        boolean conainsLogo = semail.contains(CID_ATTACHMENT_LOGO);
        boolean containsBanner = semail.contains(CID_ATTACHMENT_BANNER);

        if (containsBanner || conainsLogo) {
            Template template = daoService.find(Template.class, igreja.getChave());

            if (containsBanner && template.getBanner() != null) {
                attachments.add(new MensagemEmailDTO.Anexo(
                        EntityFileManager.get(template.getBanner(), "dados").getAbsolutePath(),
                        MensagemEmailDTO.TipoAnexo.ARQUIVO
                ));
                attachmentsNames.add(template.getBanner().getNome());
            }

            if (conainsLogo && template.getLogoGrande() != null) {
                attachments.add(new MensagemEmailDTO.Anexo(
                        EntityFileManager.get(template.getLogoGrande(), "dados").getAbsolutePath(),
                        MensagemEmailDTO.TipoAnexo.ARQUIVO
                ));
                attachmentsNames.add(template.getLogoGrande().getNome());
            }
        }

        return new MensagemEmailDTO(ssubject, semail,
                attachments.toArray(new MensagemEmailDTO.Anexo[0]),
                attachmentsNames.toArray(new String[0]));
    }

    public String formataHora(Date data, String igreja, String timezone) {
        return format(data, (String) paramService.get(igreja, TipoParametro.FORMATO_HORA), timezone);
    }

    public String formataDataHora(Date data, String igreja, String timezone) {
        return format(data, (String) paramService.get(igreja, TipoParametro.FORMATO_DATA_HORA), timezone);
    }

    public String formataData(Date data, String igreja, String timezone) {
        return format(data, (String) paramService.get(igreja, TipoParametro.FORMATO_DATA), timezone);
    }

    private String format(Date data, String pattern, String timezone) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone(timezone));
        return formatter.format(data);
    }
}
