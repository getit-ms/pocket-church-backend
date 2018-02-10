/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.domain.Funcionalidade;
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
import java.util.*;

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
    private DAOService daoService;

    private String chaveIgreja;
    private String chaveDispositivo;
    private Long idMembro;
    private boolean admin;
    private Long idUsuario;
    private List<Integer> funcionalidades;
    
    private boolean loaded;

    private static final Set<String> DISPOSITIVOS_REGISTRANDO = new HashSet<String>();
    
    public void load(String authorization){
        Number creation = null;
        if (!StringUtil.isEmpty(authorization)){
            try{
                JWTManager.JWTReader reader = JWTManager.reader(authorization);
                chaveIgreja = (String) reader.get("igreja");
                chaveDispositivo = (String) reader.get("dispositivo");
                idMembro = toLong(reader.get("membro"));
                admin = Boolean.valueOf(String.valueOf(reader.get("admin")));
                idUsuario = toLong("usuario");
                funcionalidades = (List<Integer>) reader.get("funcionalidades");
                creation = (Number) reader.get("creation");
            }catch(Exception e){
                e.printStackTrace();
                throw new ServiceException("mensagens.MSG-403");
            }
        }
        
        if (StringUtil.isEmpty(chaveIgreja)){
            chaveIgreja = get("Igreja");
            
            if (StringUtil.isEmpty(chaveIgreja)){
                throw new ServiceException("mensagens.MSG-403");
            }
        }

        Igreja igreja = daoService.find(Igreja.class, chaveIgreja);

        if (igreja.isInativa()) {
            throw new ServiceException("O aplicativo foi desativado. Entre em contato com a igreja para mais detalhes");
        }

        if (igreja.isBloqueada() && admin) {
            throw new ServiceException("O aplicativo está bloqueado. Entre em contato com a GET IT para mais detalhes.");
        }
        
        String uuid = getUUID();
        if (!StringUtil.isEmpty(uuid) && (StringUtil.isEmpty(chaveDispositivo) || !chaveDispositivo.startsWith(uuid))){
            String oldCD = chaveDispositivo;
            
            String newCD = uuid + "@" + chaveIgreja;
            
            Dispositivo dispositivo = daoService.find(Dispositivo.class, newCD);
            
            boolean processa = true;

            if (dispositivo == null){
                synchronized (DISPOSITIVOS_REGISTRANDO){
                    processa = !DISPOSITIVOS_REGISTRANDO.contains(newCD);

                    if (processa){
                        DISPOSITIVOS_REGISTRANDO.add(newCD);
                    }
                }

                if (processa){
                    dispositivo = createDispositivo(uuid);
                }
            }else{
                chaveDispositivo = newCD;
                synchronized (DISPOSITIVOS_REGISTRANDO){
                    DISPOSITIVOS_REGISTRANDO.remove(dispositivo);
                }
            }
            
            if (!StringUtil.isEmpty(oldCD)){
                if (processa){
                    daoService.execute(QueryAcesso.MIGRA_SENT_NOTIFICATIONS.create(oldCD, newCD));

                    Dispositivo old = daoService.find(Dispositivo.class, oldCD);
                    if (old != null){
                        dispositivo.registerToken(old.getTipo(), old.getPushkey(), old.getVersao());
                        dispositivo.setMembro(old.getMembro());

                        daoService.update(dispositivo);

                        if (dispositivo.isRegistrado()){
                            daoService.execute(QueryAcesso.UNREGISTER_OLD_DEVICES.create(dispositivo.getPushkey(), newCD));
                        }
                    }

                    set();
                } else {
                    chaveDispositivo = oldCD;
                }
            }else{
                chaveDispositivo = newCD;
            }
            
            if (dispositivo != null && admin != dispositivo.isAdministrativo()){
                admin = dispositivo.isAdministrativo();
                set();
            }
        } else if (StringUtil.isEmpty(uuid) && StringUtil.isEmpty(chaveDispositivo)) {
            createDispositivo(UUID.randomUUID().toString());
        } else if (!admin) {
            switch (dispositivoService.verificaContingencia(chaveDispositivo)) {
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
        
        if (deprecated || funcionalidades == null){
            refreshFuncionalidades();
            set();
        }
    }
    
    private Dispositivo createDispositivo(String uuid){
        Dispositivo dispositivo = daoService.update(new Dispositivo(uuid, daoService.find(Igreja.class, chaveIgreja)));
        daoService.update(preparaPreferencias(new Preferencias(dispositivo)));
        
        chaveDispositivo = uuid + "@" + chaveIgreja;
        
        return dispositivo;
    }
    
    private Preferencias preparaPreferencias(Preferencias preferencias){
        preferencias.setMinisteriosInteresse(daoService.findWith(QueryAcesso.MINISTERIOS_ATIVOS.create(chaveIgreja)));
        return preferencias;
    }
    
    private void load(){
        if (!loaded){
            loaded = true;
            load(get("Authorization"));
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
            manager.header("Set-Authorization", JWTManager.writer().
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
    
    public void login(Long idMembro, boolean admin){
        this.idMembro = idMembro;
        this.admin = admin;
        
        this.refreshFuncionalidades();
        
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
        
        set();
    }
    
    public String getUUID() {
        return get("Dispositivo");
    }
}
