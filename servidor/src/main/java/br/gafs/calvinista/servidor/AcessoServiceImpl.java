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
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

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
    private SessaoBean sessaoBean;

    @Override
    public Usuario admin(String username, String password) {
        Usuario usuario = daoService.findWith(QueryAcesso.AUTENTICA_USUARIO.createSingle(username, password));
        
        if (usuario != null){
            sessaoBean.admin(usuario.getId());
            return usuario;
        }
        
        throw new ServiceException("mensagens.MSG-600");
    }

    @Override
    public void registerPush(TipoDispositivo tipoDispositivo, String pushKey, String version) {
        Dispositivo dispositivo = daoService.find(Dispositivo.class, sessaoBean.getChaveDispositivo());
        dispositivo.registerToken(tipoDispositivo, pushKey, version);
        daoService.update(dispositivo);
        sessaoBean.dispositivo(dispositivo.getUuid());
    }

    @Override
    public Preferencias buscaPreferencis() {
        return daoService.find(Preferencias.class, sessaoBean.getChaveDispositivo());
    }
    
    @Override
    public List<Ministerio> buscaMinisterios(){
        return daoService.findWith(QueryAcesso.MINISTERIOS_ATIVOS.create(sessaoBean.getChaveIgreja()));
    }

    
    @Override
    public Preferencias salva(Preferencias preferencias) {
        if (sessaoBean.getIdMembro() != null){
            List<Preferencias> prefs = daoService.findWith(QueryAdmin.PREFERENCIAS_POR_MEMBRO.
                    create(sessaoBean.getIdMembro(), sessaoBean.getChaveIgreja()));
            
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
        Dispositivo dispositivo = daoService.find(Dispositivo.class, sessaoBean.getChaveDispositivo());
        dispositivo.setMembro(null);
        daoService.update(dispositivo);
        sessaoBean.logout();
    }

    @Override
    public Membro refreshLogin() {
        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));
        if (membro != null && membro.isMembro()){
            sessaoBean.login(membro.getId());
            return membro;
        }
        
        throw new ServiceException("mensagens.MSG-403");
    }

    @Override
    public Usuario refreshAdmin() {
        Usuario usuario = daoService.find(Usuario.class, sessaoBean.getIdUsuario());
        if (usuario != null){
            sessaoBean.admin(usuario.getId());
            return usuario;
        }
        
        throw new ServiceException("mensagens.MSG-403");
    }
    
    @Override
    public Membro login(String username, String password){
        Membro membro = daoService.findWith(QueryAcesso.AUTENTICA_MEMBRO.createSingle(sessaoBean.getChaveIgreja(), username, password));
        
        if (membro != null && membro.isMembro()){
            Dispositivo dispositivo = daoService.find(Dispositivo.class, sessaoBean.getChaveDispositivo());
            dispositivo.setMembro(membro);
            daoService.update(dispositivo);
            
            sessaoBean.login(membro.getId());
            return membro;
        }
        
        throw new ServiceException("mensagens.MSG-606");
    }

    @Override
    public List<Funcionalidade> getFuncionalidadesMembro() {
        return daoService.findWith(QueryAcesso.FUNCIONALIDADES_MEMBRO.
                create(sessaoBean.getIdMembro(), sessaoBean.getChaveIgreja()));
    }

    @Override
    public List<Funcionalidade> getTodasFuncionalidadesAdmin() {
        return daoService.findWith(QueryAcesso.TODAS_FUNCIONALIDADES_ADMIN.
                create(sessaoBean.getIdMembro(), sessaoBean.getChaveIgreja()));
    }

    @Override
    public void alteraSenha(Membro entidade) {
        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));
        
        membro.setNovaSenha(entidade.getNovaSenha());
        membro.setConfirmacaoSenha(entidade.getConfirmacaoSenha());
        
        membro.alteraSenha();
        
        daoService.update(membro);
    }

    @Override
    public void solicitaRedefinicaoSenha(String email) {
        Membro membro = daoService.findWith(QueryAdmin.MEMBRO_POR_EMAIL_IGREJA.createSingle(email, sessaoBean.getChaveIgreja()));
        
        if (membro == null || !membro.isMembro()){
            throw new ServiceException("mensagens.MSG-037");
        }
        
        String jwt = JWTManager.writer().map("igreja", membro.getIgreja().getId()).map("membro", membro.getId()).build();
        
        String subject = MensagemUtil.getMensagem("email.redefinir_senha.subject", membro.getIgreja().getLocale());
            String title = MensagemUtil.getMensagem("email.redefinir_senha.message.title", membro.getIgreja().getLocale(), 
                    membro.getNome());
            String text = MensagemUtil.getMensagem("email.redefinir_senha.message.text", membro.getIgreja().getLocale());
            String linkUrl = MensagemUtil.getMensagem("email.redefinir_senha.message.link.url", membro.getIgreja().getLocale(), jwt, membro.getIgreja().getChave());
            String linkText = MensagemUtil.getMensagem("email.redefinir_senha.message.link.text", membro.getIgreja().getLocale());
            
        mensagemService.sendNow(
                MensagemUtil.email(daoService.find(Institucional.class, membro.getIgreja().getChave()), subject,
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
