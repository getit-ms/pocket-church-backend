/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.entity.domain.TipoDispositivo;
import br.gafs.calvinista.sessao.SessionDataManager;
import br.gafs.calvinista.util.JWTManager;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Gabriel
 */
@Named
@RequestScoped
public class SessaoBean implements Serializable {

    private static final long TIMEOUT = 3 * DateUtil.MILESIMOS_POR_DIA;

    @EJB
    private JWTManager jwtManager;

    @EJB
    private DispositivoService dispositivoService;

    @EJB
    private IgrejaService igrejaService;

    @EJB
    private DAOService daoService;

    @Inject
    private SessionDataManager manager;

    private String chaveIgreja;
    private String chaveDispositivo;
    private Long idMembro;
    private boolean admin;
    private Long idUsuario;
    private List<Integer> funcionalidades;

    private boolean loaded;

    public void load(){
        if (!loaded){
            loaded = true;

            Number creation = null;
            String authorization = get("Authorization");
            if (!StringUtil.isEmpty(authorization)){
                try{
                    JWTManager.JWTReader reader = jwtManager.reader(authorization);
                    chaveIgreja = (String) reader.get("igreja");
                    chaveDispositivo = (String) reader.get("dispositivo");
                    idMembro = toLong(reader.get("membro"));
                    admin = Boolean.valueOf(String.valueOf(reader.get("admin")));
                    idUsuario = toLong("usuario");
                    funcionalidades = (List<Integer>) reader.get("funcionalidades");
                    creation = (Number) reader.get("creation");
                }catch(Exception e){
                    throw new ServiceException("mensagens.MSG-403", e);
                }
            } else {
                if (StringUtil.isEmpty(chaveIgreja)) {
                    chaveIgreja = get("Igreja");

                    if (StringUtil.isEmpty(chaveIgreja)) {
                        throw new ServiceException("mensagens.MSG-403");
                    }
                }

                if (StringUtil.isEmpty(chaveDispositivo)) {
                    String dispositivo = get("Dispositivo");

                    if (StringUtil.isEmpty(dispositivo)) {
                        chaveDispositivo = UUID.randomUUID().toString() + "@" + chaveIgreja;
                    } else {
                        chaveDispositivo = dispositivo + "@" + chaveIgreja;
                    }
                }
            }

            igrejaService.checkStatusIgreja(chaveIgreja, admin);

            dispositivoService.registraAcesso(chaveDispositivo);

            if (!admin) {
                switch (DispositivoService.verificaContingencia(chaveDispositivo)) {
                    case FORCE_REGISTER:
                        manager.header("Force-Register", "true");
                        break;
                    case FULL_RESET:
                        manager.header("Force-Reset", "true");
                        break;
                }
            }

            boolean deprecated = creation == null ||
                    creation.longValue() + TIMEOUT < System.currentTimeMillis();

            if (deprecated || funcionalidades == null) {
                refreshFuncionalidades();
                set();
            }

        }
    }


    private static Long toLong(Object value){
        if (value instanceof Number){
            return ((Number) value).longValue();
        }

        return null;
    }

    public void refreshFuncionalidades(){
        funcionalidades = new ArrayList<Integer>();
        if (idMembro != null){
            List<Funcionalidade> funcs;
            if (admin){
                funcs = daoService.
                        findWith(QueryAcesso.FUNCIONALIDADES_MEMBRO_ADMIN.create(idMembro, chaveIgreja));
            }else{
                funcs = daoService.
                        findWith(QueryAcesso.FUNCIONALIDADES_MEMBRO_APP.create(idMembro, chaveIgreja));
            }

            for (Funcionalidade funcionalidade : funcs){
                funcionalidades.add(funcionalidade.getCodigo());
            }
        }
    }

    private void set(){
        load();

        if (idUsuario != null || idMembro != null){
            manager.header("Set-Authorization", jwtManager.writer().
                    map("igreja", chaveIgreja).
                    map("dispositivo", chaveDispositivo).
                    map("membro", idMembro).
                    map("admin", admin).
                    map("creation", System.currentTimeMillis()).
                    map("usuario", idUsuario).
                    map("funcionalidades", funcionalidades).build());
        }
    }

    private String get(String key){
        String head = manager.header(key);
        if (StringUtil.isEmpty(head)){
            return manager.parameter(key);
        }
        return head;
    }

    public String getChaveDispositivo() {
        load();
        return chaveDispositivo;
    }

    public String getChaveIgreja() {
        load();
        return chaveIgreja;
    }

    public boolean temPermissao(Funcionalidade func){
        load();

        return funcionalidades.contains(func.getCodigo());
    }

    public List<Funcionalidade> getFuncionalidades(){
        List<Funcionalidade> funcs = new ArrayList<Funcionalidade>();
        for (Funcionalidade func : Funcionalidade.values()){
            if (temPermissao(func)){
                funcs.add(func);
            }
        }
        return funcs;
    }

    public Long getIdMembro() {
        load();
        return idMembro;
    }

    public Long getIdUsuario() {
        load();
        return idUsuario;
    }

    public boolean isAdmin() {
        load();
        return admin;
    }

    public void dispositivo(boolean admin){
        this.admin = admin;

        set();
    }

    public void login(Long idMembro, TipoDispositivo tipoDispositivo, String version){
        this.idMembro = idMembro;
        this.admin = TipoDispositivo.PC.equals(tipoDispositivo);

        this.refreshFuncionalidades();

        dispositivoService.registraLogin(chaveDispositivo, idMembro, tipoDispositivo, version);

        set();
    }

    public void admin(Long idUsuario){
        this.idUsuario = idUsuario;

        set();
    }

    public void logout(){
        this.idUsuario = null;
        this.idMembro = null;
        this.funcionalidades.clear();

        dispositivoService.registraLogout(chaveDispositivo);

        set();
    }

    public void refresh() {
        this.refreshFuncionalidades();

        if (idMembro != null) {
            dispositivoService.registraLogin(chaveDispositivo, idMembro, null, null);
        }

        set();
    }
}
