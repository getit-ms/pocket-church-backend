/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor;

import br.gafs.calvinista.sessao.SessionDataManager;
import br.gafs.calvinista.dao.QueryAcesso;
import br.gafs.calvinista.dao.QueryAdmin;
import br.gafs.calvinista.entity.Dispositivo;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.Preferencias;
import br.gafs.calvinista.entity.domain.Funcionalidade;
import br.gafs.calvinista.util.JWTManager;
import br.gafs.dao.DAOService;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.string.StringUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Gabriel
 */
@Named
@RequestScoped
public class SessaoBean implements Serializable {
    
    @Inject
    private SessionDataManager manager;
    
    @EJB
    private DAOService daoService;
    
    private String chaveIgreja;
    private String chaveDispositivo;
    private Long idMembro;
    private boolean admin;
    private Long idUsuario;
    private List<Integer> funcionalidades;
    
    private boolean loaded;
    
    private void load(){
        if (!loaded){
            loaded = true;
            
            String authorization = get("Authorization");

            if (!StringUtil.isEmpty(authorization)){
                try{
                    JWTManager.JWTReader reader = JWTManager.reader(authorization);
                    chaveIgreja = (String) reader.get("igreja");
                    chaveDispositivo = (String) reader.get("dispositivo");
                    idMembro = toLong(reader.get("membro"));
                    admin = Boolean.valueOf(String.valueOf(reader.get("admin")));
                    idUsuario = toLong("usuario");
                    funcionalidades = (List<Integer>) reader.get("funcionalidades");
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
            
            String dispositivo = get("Dispositivo");
            if (!StringUtil.isEmpty(dispositivo) &&
                    !(dispositivo + "@" + chaveIgreja).equals(chaveDispositivo)){
                dispositivo(dispositivo);
            }
            
            if (funcionalidades == null){
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
            List<Funcionalidade> funcs = daoService.
                                findWith(QueryAcesso.FUNCIONALIDADES_MEMBRO.create(idMembro, chaveIgreja));
            for (Funcionalidade funcionalidade : funcs){
                funcionalidades.add(funcionalidade.getCodigo());
            }
        }
    }
    
    public void dispositivo(String uuid) {
        String chaveDispositivo = uuid + "@" + chaveIgreja;
        Dispositivo dispositivo = daoService.find(Dispositivo.class, chaveDispositivo);

        if (dispositivo == null){
            synchronized (SessaoBean.class){
                dispositivo = daoService.find(Dispositivo.class, chaveDispositivo);

                if (dispositivo == null){
                    Igreja igreja = daoService.find(Igreja.class, chaveIgreja);
                    dispositivo = daoService.update(new Dispositivo(uuid, igreja));
                    daoService.update(preparaPreferencias(new Preferencias(dispositivo)));
                }
            }
        }

        this.chaveDispositivo = chaveDispositivo;
        this.admin = dispositivo.isAdministrativo();

        set();
    }
    
    private Preferencias preparaPreferencias(Preferencias preferencias){
        preferencias.setMinisteriosInteresse(daoService.findWith(QueryAcesso.MINISTERIOS_ATIVOS.create(getChaveIgreja())));
        return preferencias;
    }
    
    private void set(){
        load();
        
        manager.header("Set-Authorization", JWTManager.writer().
                map("igreja", chaveIgreja).
                map("dispositivo", chaveDispositivo).
                map("membro", idMembro).
                map("admin", admin).
                map("usuario", idUsuario).
                map("funcionalidades", funcionalidades).build());
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

    public void login(Long idMembro){
        this.idMembro = idMembro;
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
    
}
