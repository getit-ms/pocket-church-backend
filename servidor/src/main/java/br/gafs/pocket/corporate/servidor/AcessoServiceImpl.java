/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.servidor;

import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.pocket.corporate.dao.QueryAcesso;
import br.gafs.pocket.corporate.dao.QueryAdmin;
import br.gafs.pocket.corporate.dto.CalvinEmailDTO;
import br.gafs.pocket.corporate.dto.FiltroEmailDTO;
import br.gafs.pocket.corporate.dto.MenuDTO;
import br.gafs.pocket.corporate.entity.*;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.pocket.corporate.security.AllowColaborador;
import br.gafs.pocket.corporate.security.Audit;
import br.gafs.pocket.corporate.security.AuditoriaInterceptor;
import br.gafs.pocket.corporate.service.AcessoService;
import br.gafs.pocket.corporate.service.ArquivoService;
import br.gafs.pocket.corporate.service.MensagemService;
import br.gafs.pocket.corporate.util.JWTManager;
import br.gafs.pocket.corporate.util.MensagemUtil;
import br.gafs.util.senha.SenhaUtil;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.util.*;

/**
 *
 * @author Gabriel
 */
@Stateless
@Local(AcessoService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class})
public class AcessoServiceImpl implements AcessoService {

    @EJB
    private DAOService daoService;
    
    @EJB
    private MensagemService mensagemService;

    @EJB
    private DispositivoService dispositivoService;

    @EJB
    private ArquivoService arquivoService;

    @Inject
    private SessaoBean sessaoBean;

    @Audit
    @Override
    public Usuario admin(String username, String password) {
        Usuario usuario = daoService.findWith(QueryAcesso.AUTENTICA_USUARIO.createSingle(username, password));
        
        if (usuario != null){
            sessaoBean.admin(usuario.getId());
            return usuario;
        }
        
        throw new ServiceException("mensagens.MSG-600");
    }

    @Audit
    @Override
    public void registerPush(TipoDispositivo tipoDispositivo, String pushKey, String version) {
        dispositivoService.register(sessaoBean.getChaveDispositivo(), tipoDispositivo, pushKey, version);
    }

    private Dispositivo dispositivo() {
        Dispositivo dispositivo = daoService.find(Dispositivo.class, sessaoBean.getChaveDispositivo());

        if (dispositivo.getColaborador() == null && sessaoBean.getIdColaborador() != null){
            dispositivo.setColaborador(daoService.find(Colaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador())));
            dispositivo = daoService.update(dispositivo);
        }else if (dispositivo.getColaborador() != null && sessaoBean.getIdColaborador() == null){
            dispositivo.setColaborador(null);
            dispositivo = daoService.update(dispositivo);
        }
        
        return dispositivo;
    }

    @Override
    public Preferencias buscaPreferencis() {
        return daoService.find(Preferencias.class, dispositivo().getChave());
    }
    
    @Override
    @AllowColaborador
    public void trocaFoto(Arquivo arquivo) {
        Arquivo entidade = daoService.find(Arquivo.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), arquivo.getId()));

        if (!entidade.isUsed()) {

            arquivoService.registraUso(arquivo.getId());

            Colaborador colaborador = daoService.find(Colaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador()));

            colaborador.setFoto(entidade);

            daoService.update(colaborador);
        } else {
            throw new ServiceException("mensagens.MSG-403");
        }
    }

    @Override
    public MenuDTO buscaMenu(int versaoMajor, int versaoMinor, int versaoBugfix) {
        List<Funcionalidade> funcionalidades = getFuncionalidadesCompativeis(versaoMajor, versaoMinor, versaoBugfix);

        MenuDTO root = new MenuDTO();

        Map<Long, MenuDTO> menuMap = new HashMap<Long, MenuDTO>();

        List<Menu> menus = daoService.findWith(QueryAdmin.MENUS_EMPRESA_FUNCIONALIDADES
                .create(sessaoBean.getChaveEmpresa(), funcionalidades));

        for (Menu menu : menus) {
            MenuDTO dto = new MenuDTO(menu.getNome(), menu.getIcone(), menu.getOrdem(),
                    menu.getLink(), 0, menu.getFuncionalidade(), new ArrayList<MenuDTO>());

            if (menu.getFuncionalidade() == Funcionalidade.NOTIFICACOES) {
                dto.setNotificacoes(mensagemService.countNotificacoesNaoLidas(
                        sessaoBean.getChaveEmpresa(),
                        sessaoBean.getChaveDispositivo(),
                        sessaoBean.getIdColaborador()
                ));
            }

            if (menu.getMenuPai() == null) {
                menuMap.put(menu.getId(), dto);
                root.add(dto);
            } else {
                if (!menuMap.containsKey(menu.getMenuPai().getId())) {
                    MenuDTO dtoPai = new MenuDTO(menu.getMenuPai().getNome(),
                            menu.getMenuPai().getIcone(), menu.getMenuPai().getOrdem(), menu.getMenuPai().getLink(), 0,
                            menu.getMenuPai().getFuncionalidade(), new ArrayList<MenuDTO>());
                    menuMap.put(menu.getMenuPai().getId(), dtoPai);
                    root.add(dtoPai);
                }

                menuMap.get(menu.getMenuPai().getId()).add(dto);
            }
        }

        return root;
    }

    private List<Funcionalidade> getFuncionalidadesCompativeis(int versaoMajor, int versaoMinor, int versaoBugfix) {
        List<Funcionalidade> funcionalidades = new ArrayList<Funcionalidade>();

        funcionalidades.addAll(Funcionalidade.FUNCIONALIDADES_FIXAS);

        if (sessaoBean.getIdColaborador() != null) {
            funcionalidades.addAll(getFuncionalidadesColaborador());
        }

        int versaoApp = versaoMajor * 10000 + versaoMinor * 100 + versaoBugfix;

        Iterator<Funcionalidade> iterator = funcionalidades.iterator();
        while (iterator.hasNext()) {
            Funcionalidade func = iterator.next();

            int versaoMinima = func.getVersaoMajor() * 10000 + func.getVersaoMinor() * 100 + func.getVersaoBugfix();
            if (versaoMinima > versaoApp) {
                iterator.remove();
            }
        }

        return funcionalidades;
    }


    @Audit
    @Override
    public Preferencias salva(Preferencias preferencias) {
        if (sessaoBean.getIdColaborador() != null){
            Boolean dadosDisponiveis = preferencias.getDadosDisponiveis();
            if (dadosDisponiveis != null){
                Colaborador colaborador = daoService.find(Colaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador()));
                colaborador.setDesejaDisponibilizarDados(dadosDisponiveis);
                daoService.update(colaborador);
            }
            
            List<Preferencias> prefs = daoService.findWith(QueryAdmin.PREFERENCIAS_POR_COLABORADOR.
                    create(sessaoBean.getIdColaborador(), sessaoBean.getChaveEmpresa()));
            
            for (Preferencias pref : prefs){
                preferencias.copia(pref);
                daoService.update(preferencias);
            }
            
            return daoService.update(preferencias);
        }else{
            return daoService.update(preferencias);
        }
    }

    @Audit
    @Override
    public void logout() {
        Dispositivo dispositivo = daoService.find(Dispositivo.class, sessaoBean.getChaveDispositivo());
        
        if (dispositivo != null){
            dispositivo.setColaborador(null);
            daoService.update(dispositivo);
        }
        
        sessaoBean.logout();
    }

    @Audit
    @Override
    public Colaborador refreshLogin() {
        Colaborador colaborador = daoService.find(Colaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador()));
        if (colaborador != null && colaborador.isColaborador()){
            sessaoBean.login(colaborador.getId(), sessaoBean.isAdmin());
            return colaborador;
        }
        
        throw new ServiceException("mensagens.MSG-403");
    }

    @Audit
    @Override
    public Usuario refreshAdmin() {
        Usuario usuario = daoService.find(Usuario.class, sessaoBean.getIdUsuario());
        if (usuario != null){
            sessaoBean.admin(usuario.getId());
            return usuario;
        }
        
        throw new ServiceException("mensagens.MSG-403");
    }
    
    @Audit
    @Override
    public Colaborador login(String username, String password, TipoDispositivo tipo, String version){
        Colaborador colaborador = daoService.findWith(QueryAcesso.AUTENTICA_COLABORADOR.createSingle(sessaoBean.getChaveEmpresa(), username, password));
        
        if (colaborador != null && colaborador.isColaborador()){
            Dispositivo dispositivo = dispositivo();
            dispositivo.setColaborador(colaborador);
            daoService.update(dispositivo);
            
            registerPush(tipo, null, version);
            
            sessaoBean.login(colaborador.getId(), TipoDispositivo.PC.equals(tipo));
            return colaborador;
        }
        
        throw new ServiceException("mensagens.MSG-606");
    }

    @Override
    public List<Funcionalidade> getFuncionalidadesColaborador() {
        return sessaoBean.getFuncionalidades();
    }

    @Override
    public List<Funcionalidade> getTodasFuncionalidadesAdmin() {
        return daoService.findWith(QueryAcesso.TODAS_FUNCIONALIDADES_ADMIN.
                create(sessaoBean.getChaveEmpresa()));
    }

    @Audit
    @Override
    public void alteraSenha(Colaborador entidade) {
        Colaborador colaborador = daoService.find(Colaborador.class, new RegistroEmpresaId(sessaoBean.getChaveEmpresa(), sessaoBean.getIdColaborador()));
        
        colaborador.setNovaSenha(entidade.getNovaSenha());
        colaborador.setConfirmacaoSenha(entidade.getConfirmacaoSenha());
        
        colaborador.alteraSenha();
        
        daoService.update(colaborador);
    }

    @Audit
    @Override
    public void solicitaRedefinicaoSenha(String email) {
        Colaborador colaborador = daoService.findWith(QueryAdmin.COLABORADOR_POR_EMAIL_EMPRESA.createSingle(email, sessaoBean.getChaveEmpresa()));
        
        if (colaborador == null || !colaborador.isColaborador()){
            throw new ServiceException("mensagens.MSG-037");
        }
        
        String jwt = JWTManager.writer().map("empresa", colaborador.getEmpresa().getId()).map("colaborador", colaborador.getId()).build()
                .replace("/", "%2F")
                .replace("-", "%2D")
                .replace(".", "%2E")
                .replace("=", "%3D")
                .replace("_", "%5F");
        
        String subject = MensagemUtil.getMensagem("email.redefinir_senha.subject", colaborador.getEmpresa().getLocale());
            String title = MensagemUtil.getMensagem("email.redefinir_senha.message.title", colaborador.getEmpresa().getLocale(),
                    colaborador.getNome());
            String text = MensagemUtil.getMensagem("email.redefinir_senha.message.text", colaborador.getEmpresa().getLocale());
            String linkUrl = MensagemUtil.getMensagem("email.redefinir_senha.message.link.url", colaborador.getEmpresa().getLocale(), jwt, colaborador.getEmpresa().getChave());
            String linkText = MensagemUtil.getMensagem("email.redefinir_senha.message.link.text", colaborador.getEmpresa().getLocale());
            
        mensagemService.sendNow(
                MensagemUtil.email(daoService.find(Institucional.class, colaborador.getEmpresa().getChave()), subject,
                        new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, linkUrl, linkText), Collections.EMPTY_LIST)),
                new FiltroEmailDTO(colaborador.getEmpresa(), colaborador.getId()));
    }

    @Audit
    @Override
    public Colaborador redefineSenha(String jwt) {
        JWTManager.JWTReader reader = JWTManager.reader(jwt);
        
        Colaborador colaborador = daoService.find(Colaborador.class, new RegistroEmpresaId(
                            (String) reader.get("empresa"),
                            ((Number) reader.get("colaborador")).longValue()));
        
        if (colaborador != null || !colaborador.isColaborador()){
            String novaSenha = SenhaUtil.geraSenha(8);
            colaborador.setSenha(SenhaUtil.encryptSHA256(novaSenha));
            colaborador = daoService.update(colaborador);
            
            String subject = MensagemUtil.getMensagem("email.nova_senha.subject", 
                    colaborador.getEmpresa().getLocale());
            String title = MensagemUtil.getMensagem("email.nova_senha.message.title", 
                    colaborador.getEmpresa().getLocale(), colaborador.getNome());
            String text = MensagemUtil.getMensagem("email.nova_senha.message.text", 
                    colaborador.getEmpresa().getLocale(), colaborador.getEmpresa().getNome());
            
            mensagemService.sendNow(
                    MensagemUtil.email(daoService.find(Institucional.class, colaborador.getEmpresa().getChave()), subject,
                            new CalvinEmailDTO(new CalvinEmailDTO.Manchete(title, text, "javascript:void(0)", novaSenha), Collections.EMPTY_LIST)),
                    new FiltroEmailDTO(colaborador.getEmpresa(), colaborador.getId()));
        }
        
        return colaborador;
    }    
    
}
