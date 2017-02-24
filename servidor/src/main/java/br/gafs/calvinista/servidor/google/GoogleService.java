/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor.google;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.VideoDTO;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.SessaoBean;
import br.gafs.util.string.StringUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@Stateless
public class GoogleService {
    
    public static List<String> YOUTUBE_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("YOUTUBE_SCOPES").split("\\s*,\\s*"));
    
    private static final File GOOGLE_STORE_DIR = new File(ResourceBundleUtil._default().getPropriedade("GOOGLE_STORE_DIR"));
    
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    
    static {
        if (!GOOGLE_STORE_DIR.exists()){
            GOOGLE_STORE_DIR.mkdirs();
        }
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(new File(GOOGLE_STORE_DIR, "store"));
        } catch (IOException ex) {
            Logger.getLogger(GoogleService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @EJB
    private ParametroService paramService;
    
    @Inject
    private SessaoBean sessao;
    
    
    private GoogleAuthorizationCodeFlow.Builder flow(Collection<String> scopes) throws IOException{
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                (String) paramService.get(sessao.getChaveIgreja(), TipoParametro.GOOGLE_OAUTH_CLIENT_KEY), 
                (String) paramService.get(sessao.getChaveIgreja(), TipoParametro.GOOGLE_OAUTH_SECRET_KEY),
                scopes).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline");
    }
    
    public String getURLAutorizacaoYouTube() throws IOException {
        return flow(YOUTUBE_SCOPES).build().newAuthorizationUrl().
                setRedirectUri(MessageFormat.format(ResourceBundleUtil._default().
                        getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), sessao.getChaveIgreja())).
                setState(sessao.getChaveIgreja()).build();
    }

    public Credential saveCredentialsYouTube(String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(YOUTUBE_SCOPES).build();
        
        TokenResponse resp = flow.newTokenRequest(code).
                setRedirectUri(MessageFormat.format(ResourceBundleUtil._default().
                        getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), sessao.getChaveIgreja())).execute();
        
        return flow.createAndStoreCredential(resp, sessao.getChaveIgreja());
    }
    
    public Credential loadCredentialsYouTube() throws IOException {
        return loadCredentialsYouTube(sessao.getChaveIgreja());
    }
    
    private Credential loadCredentialsYouTube(String chaveIgreja) throws IOException {
        return flow(YOUTUBE_SCOPES).
                addRefreshListener(new DataStoreCredentialRefreshListener(chaveIgreja, DATA_STORE_FACTORY)).
                build().loadCredential(chaveIgreja);
    }
    
    public String buscaIdCanal() throws IOException {
        ChannelListResponse response = connect(sessao.getChaveIgreja()).channels().list("id").setMine(true).execute();
        
        if (!response.isEmpty()){
            return response.getItems().get(0).getId();
        }
        
        return null;
    }
    
    private YouTube connect(String igreja) throws IOException {
        return new YouTube.Builder(new ApacheHttpTransport(), new JacksonFactory(), 
                loadCredentialsYouTube(igreja)).setApplicationName("Pocket Church").build();
    }
    
    public List<VideoDTO> buscaVideos() throws IOException {
        return buscaVideos(sessao.getChaveIgreja());
    }
    
    private List<VideoDTO> buscaVideos(String chave) throws IOException {
        String channelId = (String) paramService.get(chave, TipoParametro.YOUTUBE_CHANNEL_ID);
        
        if (StringUtil.isEmpty(channelId)){
            return Collections.emptyList();
        }
        
        YouTube connection = connect(chave);
        SearchListResponse response = connection.search().list("id,snippet").
                setChannelId(channelId).
                setMaxResults(30L).setType("video").setOrder("date").execute();
        
        List<VideoDTO> videos = new ArrayList<VideoDTO>();
        
        for (SearchResult result : response.getItems()){
            VideoDTO video = new VideoDTO(
                    result.getId().getVideoId(), 
                    result.getSnippet().getTitle(),
                    result.getSnippet().getDescription(),
                    result.getSnippet().getThumbnails().getDefault().getUrl(),
                    new Date(result.getSnippet().getPublishedAt().getValue()));
            
            switch (result.getSnippet().getLiveBroadcastContent()){
                case "live":
                    video.setAoVivo(true);
                    break;
                case "upcoming":
                    LiveBroadcastListResponse liveResponse = connection.liveBroadcasts().list("snippet").setId(video.getId()).execute();
                    
                    if (!liveResponse.isEmpty()){
                        video.setAgendamento(new Date(liveResponse.getItems().get(0).getSnippet().getScheduledStartTime().getValue()));
                    }
                    
                    break;
            }
            
            videos.add(video);
        }
        
        return videos;
    }

    public List<VideoDTO> buscaStreamingsAtivos(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();
        
        for (VideoDTO video : buscaVideos(chave)){
            if (video.isAoVivo()){
                videos.add(video);
            }
        }
        
        return videos;
    }

    public List<VideoDTO> buscaStreamingsAgendados(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();
        
        for (VideoDTO video : buscaVideos(chave)){
            if (video.isAgendado()){
                videos.add(video);
            }
        }
        
        return videos;
    }
    
}
