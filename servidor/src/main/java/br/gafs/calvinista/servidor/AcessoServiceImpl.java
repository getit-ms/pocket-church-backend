/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dto.CalvinEmailDTO;
import br.gafs.calvinista.dto.FiltroEmailDTO;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Institucional;
import br.gafs.calvinista.entity.Membro;
import br.gafs.calvinista.entity.Ministerio;
import br.gafs.calvinista.entity.Perfil;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.RegistroIgrejaId;
import br.gafs.calvinista.entity.Usuario;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.util.JWTManager;
import br.gafs.calvinista.util.MensagemUtil;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.util.senha.SenhaUtil;
import br.gafs.util.string.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Gabriel
 */
@Stateless
@Local(AcessoService.class)
@Interceptors(ServiceLoggerInterceptor.class)
public class AcessoServiceImpl implements AcessoService {
    
    @EJB
    private DAOService daoService;
    
    @EJB
    private MensagemService mensagemService;
    
    @Inject
    private HttpServletRequest request;

    @Override
    public Igreja getIgreja() {
        return (Igreja) request.getAttribute("igreja");
    }

    @Override
    public Membro getMembro() {
        return (Membro) request.getAttribute("membro");
    }

    @Override
    public Dispositivo getDispositivo() {
        return (Dispositivo) request.getAttribute("dispositivo");
    }

    @Override
    public Usuario getUsuario() {
        return (Usuario) request.getAttribute("usuario");
    }

    @Override
    public String admin(String username, String password) {
        Usuario usuario = daoService.findWith(QueryAcesso.AUTENTICA_USUARIO.createSingle(username, password));
        
        if (usuario != null){
            request.setAttribute("usuario", usuario);
            return JWTManager.writer().map("usuario", usuario.getId()).build();
        }
        
        throw new ServiceException("mensagens.MSG-600");
    }

    @Override
    public void registerPush(TipoDispositivo tipoDispositivo, String pushKey, String version) {
        Dispositivo dispositivo = daoService.find(Dispositivo.class, getDispositivo().getId());
        Logger.getLogger(AcessoServiceImpl.class.getName()).log(Level.WARNING, 
                "Atualizando pushkey para " + dispositivo.getId() + ": " + tipoDispositivo + " - " + pushKey + " - " + version);
        dispositivo.registerToken(tipoDispositivo, pushKey, version);
        request.setAttribute("dispositivo", daoService.update(dispositivo));
    }

    @Override
    public Preferencias buscaPreferencis() {
        Preferencias preferencias = daoService.find(Preferencias.class, getDispositivo().getChave());
        
        if (preferencias == null){
            preferencias = criaPreferenciasDispositivo();
            
            if (getMembro() != null){
                List<Preferencias> prefs = daoService.findWith(QueryAdmin.PREFERENCIAS_POR_MEMBRO.
                        create(getMembro().getId(), getMembro().getIgreja().getId()));

                if (!prefs.isEmpty()){
                    prefs.get(0).copia(preferencias);
                }
            }
        }
        
        return preferencias;
    }
    
    private Preferencias criaPreferenciasDispositivo(){
        return preparaPreferencias(new Preferencias(getDispositivo()));
    }
    
    private Preferencias preparaPreferencias(Preferencias preferencias){
        preferencias.setMinisteriosInteresse(buscaMinisterios());
        return preferencias;
    }
    
    @Override
    public List<Ministerio> buscaMinisterios(){
        return daoService.findWith(QueryAcesso.MINISTERIOS_ATIVOS.create(getIgreja().getChave()));
    }

    
    @Override
    public Preferencias salva(Preferencias preferencias) {
        if (getMembro() != null){
            List<Preferencias> prefs = daoService.findWith(QueryAdmin.PREFERENCIAS_POR_MEMBRO.
                    create(getMembro().getId(), getMembro().getIgreja().getId()));
            
            for (Preferencias pref : prefs){
                preferencias.copia(pref);
                daoService.update(preferencias);
            }
            
            return daoService.update(preferencias);
        }else{
            return daoService.update(preferencias);
        }
    }

    @Override
    public void logout() {
        Dispositivo dispositivo = getDispositivo();
        dispositivo.setMembro(null);
        daoService.update(dispositivo);
    }
    
    @Override
    public String login(String username, String password){
        Dispositivo dispositivo = getDispositivo();
        Membro membro = daoService.findWith(QueryAcesso.AUTENTICA_MEMBRO.createSingle(dispositivo.getIgreja().getChave(), username, password));
        
        if (membro != null && membro.isMembro()){
            dispositivo.setMembro(membro);
            daoService.update(dispositivo);
            
            request.setAttribute("membro", membro);
            return JWTManager.writer().map("membro", membro.getId()).build();
        }
        
        throw new ServiceException("mensagens.MSG-606");
    }

    @Override
    public void acesso(String codIgreja, String codDispositivo, String autenticacao) {
        try{
            if (!StringUtil.isEmpty(codIgreja) && !StringUtil.isEmpty(codDispositivo)){
                if (!StringUtil.isEmpty(autenticacao)){
                    JWTManager.JWTReader reader = JWTManager.reader(autenticacao);
                    String idMembro = String.valueOf(reader.get("membro"));
                    String idUsuario = String.valueOf(reader.get("usuario"));

                    if (idMembro.matches("[0-9]+")){
                        loadMembro(codIgreja, Long.parseLong(idMembro));
                    }else if (idUsuario.matches("[0-9]+")){
                        loadUsuario(Long.parseLong(idUsuario));
                    }
                }

                loadDispositivo(codIgreja, codDispositivo);
                return;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        throw new ServiceException("mensagens-MSG-603");
    }
    
    private void loadUsuario(Long idUsuario){
        Usuario usuario = daoService.find(Usuario.class, idUsuario);
        request.setAttribute("usuario", usuario);
    }
    
    private void loadDispositivo(String codIgreja, String codDispositivo) {
        Igreja igreja = daoService.find(Igreja.class, codIgreja);

        request.setAttribute("igreja", igreja);
        
        Dispositivo dispositivo = daoService.find(Dispositivo.class, 
                codDispositivo + "@" + igreja.getChave());
        
        if (dispositivo == null){
            dispositivo = daoService.update(new Dispositivo(codDispositivo, igreja));
            salva(preparaPreferencias(new Preferencias(dispositivo)));
        }
                
        if (dispositivo.getMembro() == null && getMembro() != null){
            dispositivo.setMembro(getMembro());
            dispositivo = daoService.update(dispositivo);
        }
        
        request.setAttribute("dispositivo", dispositivo);
    }

    private void loadMembro(String chaveIgreja, Long idMembro) {
        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(chaveIgreja, idMembro));
        
        request.setAttribute("membro", membro);
    }
    
    @Override
    public List<Funcionalidade> getFuncionalidades() {
        if (getUsuario() != null){
            return Arrays.asList(Funcionalidade.values());
        }
        
        if (getMembro() != null){
            Set<Funcionalidade> set = new HashSet<>(getIgreja().getFuncionalidadesAplicativo());
            if (getMembro().isAdmin()){
                List<Funcionalidade> funcsAdmin = getIgreja().getPlano().getFuncionalidadesAdmin();
                for (Perfil perfil : getMembro().getAcesso().getPerfis()){
                    for (Funcionalidade func : perfil.getFuncionalidades()){
                        if (funcsAdmin.contains(func)){
                            set.add(func);
                        }
                    }
                }
            }
            List<Funcionalidade> list = new ArrayList<>(set);
            Collections.sort(list);
            return list;
        }
        
        return Collections.emptyList();
    }

    @Override
    public void alteraSenha(Membro entidade) {
        entidade.alteraSenha();
        daoService.update(entidade);
    }

    @Override
    public void solicitaRedefinicaoSenha(String email) {
        Membro membro = daoService.findWith(QueryAdmin.MEMBRO_POR_EMAIL_IGREJA.createSingle(email, getIgreja().getChave()));
        
        if (membro == null || !membro.isMembro()){
            throw new ServiceException("mensagens.MSG-037");
        }
        
        String jwt = JWTManager.writer().map("igreja", getIgreja().getId()).map("membro", membro.getId()).build();
        
        String subject = MensagemUtil.getMensagem("email.redefinir_senha.subject", getIgreja().getLocale());
            String title = MensagemUtil.getMensagem("email.redefinir_senha.message.title", getIgreja().getLocale(), 
                    membro.getNome());
            String text = MensagemUtil.getMensagem("email.redefinir_senha.message.text", getIgreja().getLocale());
            String linkUrl = MensagemUtil.getMensagem("email.redefinir_senha.message.link.url", getIgreja().getLocale(), jwt, getIgreja().getChave());
            String linkText = MensagemUtil.getMensagem("email.redefinir_senha.message.link.text", getIgreja().getLocale());
            
        mensagemService.sendNow(
                MensagemUtil.email(daoService.find(Institucional.class, getIgreja().getChave()), subject,
                        new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, linkUrl, linkText), Collections.EMPTY_LIST)), 
                new FiltroEmailDTO(membro.getIgreja(), membro.getId()));
    }

    @Override
    public Membro redefineSenha(String jwt) {
        JWTManager.JWTReader reader = JWTManager.reader(jwt);
        
        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(
                            (String) reader.get("igreja"),
                            ((Number) reader.get("membro")).longValue()));
        
        if (membro != null || !membro.isMembro()){
            String novaSenha = SenhaUtil.geraSenha(8);
            membro.setSenha(SenhaUtil.encryptSHA256(novaSenha));
            membro = daoService.update(membro);
            
            String subject = MensagemUtil.getMensagem("email.nova_senha.subject", 
                    membro.getIgreja().getLocale());
            String title = MensagemUtil.getMensagem("email.nova_senha.message.title", 
                    membro.getIgreja().getLocale(), membro.getNome());
            String text = MensagemUtil.getMensagem("email.nova_senha.message.text", 
                    membro.getIgreja().getLocale(), membro.getIgreja().getNome());
            
            mensagemService.sendNow(
                    MensagemUtil.email(daoService.find(Institucional.class, membro.getIgreja().getChave()), subject,
                            new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, "", novaSenha), Collections.EMPTY_LIST)), 
                    new FiltroEmailDTO(membro.getIgreja(), membro.getId()));
        }
        
        return membro;
    }    
    
}
