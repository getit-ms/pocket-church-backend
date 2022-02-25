/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.dto.FiltroEmailDTO;
import br.gafs.calvinista.dto.MenuDTO;
import br.gafs.calvinista.dto.ResumoIgrejaDTO;
import br.gafs.calvinista.entity.*;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.security.AllowMembro;
import br.gafs.calvinista.security.Audit;
import br.gafs.calvinista.security.AuditoriaInterceptor;
import br.gafs.calvinista.service.AcessoService;
import br.gafs.calvinista.service.ArquivoService;
import br.gafs.calvinista.service.MensagemService;
import br.gafs.calvinista.util.JWTManager;
import br.gafs.calvinista.util.MensagemBuilder;
import br.gafs.exceptions.ServiceException;
import br.gafs.logger.ServiceLoggerInterceptor;
import br.gafs.util.senha.SenhaUtil;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Gabriel
 */
@Stateless
@Local(AcessoService.class)
@Interceptors({ServiceLoggerInterceptor.class, AuditoriaInterceptor.class})
public class AcessoServiceImpl implements AcessoService {

    @EJB
    private CustomDAOService daoService;

    @EJB
    private MensagemService mensagemService;

    @EJB
    private DispositivoService dispositivoService;

    @EJB
    private ArquivoService arquivoService;

    @EJB
    private JWTManager jwtManager;

    @EJB
    private MensagemBuilder mensagemBuilder;

    @Inject
    private SessaoBean sessaoBean;

    @Override
    public Usuario admin(String username, String password) {
        Usuario usuario = daoService.findWith(QueryAcesso.AUTENTICA_USUARIO.createSingle(username, password));

        if (usuario != null) {
            sessaoBean.admin(usuario.getId());
            return usuario;
        }

        throw new ServiceException("mensagens.MSG-600");
    }

    @Override
    public void registerPush(TipoDispositivo tipoDispositivo, String pushKey, String version) {
        dispositivoService.registraPush(sessaoBean.getChaveDispositivo(), tipoDispositivo, pushKey, version);
    }

    @Override
    public List<Funcionalidade> buscaFuncionalidadesPublicas() {
        return daoService.findWith(QueryAcesso.FUNCIONALIDADES_PUBLICAS.create(sessaoBean.getChaveIgreja()));
    }

    @Override
    public Preferencias buscaPreferencis() {
        Preferencias preferencias = daoService.find(Preferencias.class, sessaoBean.getChaveDispositivo());

        if (preferencias == null) {
            Dispositivo dispositivo = dispositivoService.getDispositivo(sessaoBean.getChaveDispositivo());
            preferencias = daoService.find(Preferencias.class, dispositivo.getChave());
        }

        return preferencias;
    }

    @Override
    public List<Ministerio> buscaMinisterios() {
        return daoService.findWith(QueryAcesso.MINISTERIOS_ATIVOS.create(sessaoBean.getChaveIgreja()));
    }

    @Override
    public boolean isExigeAceiteTermo() {
        TermoAceite termo = daoService.findWith(QueryAdmin.ULTIMO_TERMO.createSingle(sessaoBean.getChaveIgreja()));

        if (termo != null) {
            AceiteTermoMembro aceite = daoService.findWith(QueryAdmin.ULTIMO_ACEITE
                    .createSingle(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));

            return aceite == null || termo.getVersao() > aceite.getTermoAceite().getVersao();
        }

        return false;
    }

    @Override
    @AllowMembro
    public void trocaFoto(Arquivo arquivo) {
        Arquivo entidade = daoService.find(Arquivo.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), arquivo.getId()));

        if (!entidade.isUsed()) {

            arquivoService.registraUso(arquivo.getId());

            Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));

            membro.setFoto(entidade);

            daoService.update(membro);
        } else {
            throw new ServiceException("mensagens.MSG-403");
        }
    }

    @Override
    public MenuDTO buscaMenu(int versaoMajor, int versaoMinor, int versaoBugfix) {
        List<Funcionalidade> funcionalidades = getFuncionalidadesCompativeis(versaoMajor, versaoMinor, versaoBugfix);

        MenuDTO root = new MenuDTO();

        Map<Long, MenuDTO> menuMap = new HashMap<Long, MenuDTO>();

        List<Menu> menus = daoService.findWith(QueryAdmin.MENUS_IGREJA_FUNCIONALIDADES
                .create(sessaoBean.getChaveIgreja(), funcionalidades));

        for (Menu menu : menus) {
            MenuDTO dto = new MenuDTO(menu.getNome(), menu.getIcone(), menu.getOrdem(),
                    menu.getLink(), 0, menu.getFuncionalidade(), new ArrayList<MenuDTO>());

            if (menu.getFuncionalidade() == Funcionalidade.NOTIFICACOES) {
                dto.setNotificacoes(mensagemService.countNotificacoesNaoLidas(
                        sessaoBean.getChaveIgreja(),
                        sessaoBean.getChaveDispositivo(),
                        sessaoBean.getIdMembro()
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

        funcionalidades.addAll(buscaFuncionalidadesPublicas());

        if (sessaoBean.getIdMembro() != null) {
            funcionalidades.addAll(getFuncionalidadesMembro());
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
        if (sessaoBean.getIdMembro() != null) {
            Boolean dadosDisponiveis = preferencias.getDadosDisponiveis();
            if (dadosDisponiveis != null) {
                Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));
                membro.setDesejaDisponibilizarDados(dadosDisponiveis);
                daoService.update(membro);
            }

            List<Preferencias> prefs = daoService.findWith(QueryAdmin.PREFERENCIAS_POR_MEMBRO.
                    create(sessaoBean.getIdMembro(), sessaoBean.getChaveIgreja()));

            for (Preferencias pref : prefs) {
                preferencias.copia(pref);
                daoService.update(preferencias);
            }

            return daoService.update(preferencias);
        } else {
            return daoService.update(preferencias);
        }
    }

    @Override
    public void logout() {
        sessaoBean.logout();
    }

    @Override
    public Membro refreshLogin() {
        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));
        if (membro != null && membro.isMembro()) {
            sessaoBean.refresh();
            return membro;
        }

        throw new ServiceException("mensagens.MSG-403");
    }

    @Override
    public Usuario refreshAdmin() {
        Usuario usuario = daoService.find(Usuario.class, sessaoBean.getIdUsuario());
        if (usuario != null) {
            sessaoBean.admin(usuario.getId());
            return usuario;
        }

        throw new ServiceException("mensagens.MSG-403");
    }

    @Override
    public List<ResumoIgrejaDTO> inciaLogin(String username) {
        List<ResumoIgrejaDTO> igrejas = daoService.findWith(QueryAcesso.BUSCA_IGREJAS_EMAIL.create(username.toLowerCase()));

        if (igrejas.isEmpty()) {
            throw new ServiceException("mensagens.MSG-606");
        }

        return igrejas;
    }

    @Override
    public Membro login(String username, String password, TipoDispositivo tipo, String version) {
        Membro membro = daoService.findWith(QueryAcesso.AUTENTICA_MEMBRO.createSingle(sessaoBean.getChaveIgreja(), username, password));

        if (membro != null && membro.isMembro()) {
            sessaoBean.login(membro.getId(), tipo, version);

            return membro;
        }

        throw new ServiceException("mensagens.MSG-606");
    }

    @Override
    public List<Funcionalidade> getFuncionalidadesMembro() {
        return sessaoBean.getFuncionalidades();
    }

    @Override
    public List<Funcionalidade> getTodasFuncionalidadesAdmin() {
        return daoService.findWith(QueryAcesso.TODAS_FUNCIONALIDADES_ADMIN.
                create(sessaoBean.getChaveIgreja()));
    }

    @Audit
    @Override
    public void alteraSenha(Membro entidade) {
        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(sessaoBean.getChaveIgreja(), sessaoBean.getIdMembro()));

        membro.setNovaSenha(entidade.getNovaSenha());
        membro.setConfirmacaoSenha(entidade.getConfirmacaoSenha());

        membro.alteraSenha();

        daoService.update(membro);
    }

    @Override
    public void solicitaRedefinicaoSenha(String email) throws UnsupportedEncodingException {
        Membro membro = daoService.findWith(QueryAdmin.MEMBRO_POR_EMAIL_IGREJA.createSingle(email, sessaoBean.getChaveIgreja()));

        if (membro == null || !membro.isMembro()) {
            throw new ServiceException("mensagens.MSG-037");
        }

        String jwt = jwtManager.writer()
                .map("igreja", membro.getIgreja().getId())
                .map("membro", membro.getId())
                .build()
                .replace("/", "%2F")
                .replace("-", "%2D")
                .replace(".", "%2E")
                .replace("=", "%3D")
                .replace("_", "%5F");

        mensagemService.sendNow(
                mensagemBuilder.email(
                        membro.getIgreja(),
                        TipoParametro.EMAIL_SUBJECT_SOLICITAR_REDEFINICAO_SENHA,
                        TipoParametro.EMAIL_BODY_SOLICITAR_REDEFINICAO_SENHA,
                        membro.getNome(), jwt
                ),
                new FiltroEmailDTO(membro.getIgreja(), membro.getId()));
    }

    @Override
    public Membro redefineSenha(String jwt) {
        JWTManager.JWTReader reader = jwtManager.reader(jwt);

        Membro membro = daoService.find(Membro.class, new RegistroIgrejaId(
                (String) reader.get("igreja"),
                ((Number) reader.get("membro")).longValue()));

        if (membro != null && membro.isMembro()) {
            String novaSenha = SenhaUtil.geraSenha(8);
            membro.setSenha(SenhaUtil.encryptSHA256(novaSenha));
            membro = daoService.update(membro);

            mensagemService.sendNow(
                    mensagemBuilder.email(
                            membro.getIgreja(),
                            TipoParametro.EMAIL_SUBJECT_REDEFINIR_SENHA,
                            TipoParametro.EMAIL_BODY_REDEFINIR_SENHA,
                            membro.getNome(), novaSenha
                    ),
                    new FiltroEmailDTO(membro.getIgreja(), membro.getId()));

            return membro;
        }

        return null;
    }

}
