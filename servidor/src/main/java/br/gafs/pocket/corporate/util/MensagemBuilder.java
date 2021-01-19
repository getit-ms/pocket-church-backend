package br.gafs.pocket.corporate.util;

import br.gafs.dao.DAOService;
import br.gafs.file.EntityFileManager;
import br.gafs.pocket.corporate.dto.MensagemEmailDTO;
import br.gafs.pocket.corporate.entity.Empresa;
import br.gafs.pocket.corporate.entity.Template;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.service.ParametroService;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Gabriel
 */
@Singleton
public class MensagemBuilder {

    public static final String CID_ATTACHMENT_LOGO = "cid:attachment0";
    public static final String CID_ATTACHMENT_BANNER = "cid:attachment1";

    @EJB
    private DAOService daoService;

    @EJB
    private ParametroService paramService;

    public MensagemEmailDTO email(Empresa empresa, TipoParametro subject, TipoParametro email, Object... args){
        List<Object> allArgs = new ArrayList<Object>();

        allArgs.add(empresa.getChave());
        allArgs.add(empresa.getNome());
        allArgs.add(empresa.getNomeAplicativo());
        allArgs.add(CID_ATTACHMENT_LOGO);
        allArgs.add(CID_ATTACHMENT_BANNER);
        allArgs.addAll(Arrays.asList(args));

        String ssubject = paramService.get(empresa.getChave(), subject);
        String semail = MessageFormat.format(
                (String)  paramService.get(empresa.getChave(), email),
                allArgs.toArray()
        );

        List<MensagemEmailDTO.Anexo> attachments = new ArrayList<MensagemEmailDTO.Anexo>();
        List<String> attachmentsNames = new ArrayList<String>();

        boolean conainsLogo = semail.contains(CID_ATTACHMENT_LOGO);
        boolean containsBanner = semail.contains(CID_ATTACHMENT_BANNER);

        if (containsBanner || conainsLogo) {
            Template template = daoService.find(Template.class, empresa.getChave());

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

    public String formataHora(Date data, String empresa, String timezone){
        return format(data, (String) paramService.get(empresa, TipoParametro.FORMATO_HORA), timezone);
    }
    public String formataDataHora(Date data, String empresa, String timezone){
        return format(data, (String) paramService.get(empresa, TipoParametro.FORMATO_DATA_HORA), timezone);
    }
    public String formataData(Date data, String empresa, String timezone){
        return format(data, (String) paramService.get(empresa, TipoParametro.FORMATO_DATA), timezone);
    }

    private String format(Date data, String pattern, String timezone){
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone(timezone));
        return formatter.format(data);
    }
}
