/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.servidor;

import br.gafs.dao.DAOService;
import br.gafs.pocket.corporate.dao.QueryAcesso;
import br.gafs.pocket.corporate.entity.domain.Funcionalidade;
import br.gafs.pocket.corporate.entity.domain.TipoDispositivo;
import br.gafs.pocket.corporate.sessao.SessionDataManager;
import br.gafs.pocket.corporate.util.JWTManager;
import br.gafs.util.date.DateUtil;
import br.gafs.util.string.StringUtil;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel
 */
@Named
@RequestScoped
public class SessaoBean implements Serializable {

    private static final long TIMEOUT = 3 * DateUtil.MILESIMOS_POR_DIA;

    @Inject
    private SessionDataManager manager;

    @EJB
    private DispositivoService dispositivoService;

    @EJB
    private EmpresaService empresaService;

    @EJB
    private DAOService daoService;

    @EJB
    private JWTManager jwtManager;

    private String chaveEmpresa;
    private String chaveDispositivo;
    private Long idColaborador;
    private boolean admin;
    private Long idUsuario;

    private List<Integer> funcionalidades;

    private boolean loaded;

    public void load(){
        if (!loaded) {
            loaded = true;

            Number creation = null;
            String authorization = get("Authorization");
            if (!StringUtil.isEmpty(authorization)) {
                try {
                    JWTManager.JWTReader reader = jwtManager.reader(authorization);
                    chaveEmpresa = (String) reader.get("empresa");
                    chaveDispositivo = (String) reader.get("dispositivo");
                    idColaborador = toLong(reader.get("colaborador"));
                    admin = Boolean.valueOf(String.valueOf(reader.get("admin")));
                    idUsuario = toLong("usuario");
                    funcionalidades = (List<Integer>) reader.get("funcionalidades");
                    creation = (Number) reader.get("creation");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new SecurityException();
                }
            }

            empresaService.checkStatusEmpresa(chaveEmpresa, admin);

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
        if (idColaborador != null){
            List<Funcionalidade> funcs;
            if (admin){
                funcs = daoService.
                        findWith(QueryAcesso.FUNCIONALIDADES_COLABORADOR_ADMIN.create(idColaborador, chaveEmpresa));
            }else{
                funcs = daoService.
                        findWith(QueryAcesso.FUNCIONALIDADES_COLABORADOR_APP.create(idColaborador, chaveEmpresa));
            }

            for (Funcionalidade funcionalidade : funcs){
                funcionalidades.add(funcionalidade.getCodigo());
            }
        }
    }

    private void set(){
        load();

        if (idUsuario != null || idColaborador != null){
            manager.header("Set-Authorization", jwtManager.writer().
                    map("empresa", chaveEmpresa).
                    map("dispositivo", chaveDispositivo).
                    map("colaborador", idColaborador).
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

    public String getChaveEmpresa() {
        load();
        return chaveEmpresa;
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

    public Long getIdColaborador() {
        load();
        return idColaborador;
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

    public void login(Long idColaborador, TipoDispositivo tipoDispositivo, String version){
        this.idColaborador = idColaborador;
        this.admin = TipoDispositivo.PC.equals(tipoDispositivo);

        this.refreshFuncionalidades();

        dispositivoService.registraLogin(chaveDispositivo, idColaborador, tipoDispositivo, version);

        set();
    }

    public void admin(Long idUsuario){
        this.idUsuario = idUsuario;

        set();
    }

    public void logout(){
        this.idUsuario = null;
        this.idColaborador = null;
        this.funcionalidades.clear();

        set();
    }

    public void refresh() {
        this.refreshFuncionalidades();

        if (idColaborador != null) {
            dispositivoService.registraLogin(chaveDispositivo, idColaborador, null, null);
        }

        set();
    }
}
