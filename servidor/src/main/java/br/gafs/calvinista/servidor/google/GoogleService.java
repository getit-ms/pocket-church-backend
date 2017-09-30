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
    
    
    private GoogleAuthorizationCodeFlow.Builder flow(String chaveIgreja, Collection<String> scopes) throws IOException{
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                (String) paramService.get(chaveIgreja, TipoParametro.GOOGLE_OAUTH_CLIENT_KEY), 
                (String) paramService.get(chaveIgreja, TipoParametro.GOOGLE_OAUTH_SECRET_KEY),
                scopes).addRefreshListener(new DataStoreCredentialRefreshListener(chaveIgreja, DATA_STORE_FACTORY)).
                setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline");
    }
    
    public String getURLAutorizacaoYouTube(String chaveIgreja) throws IOException {
        return flow(chaveIgreja, YOUTUBE_SCOPES).setApprovalPrompt("force").build().newAuthorizationUrl().
                setRedirectUri(MessageFormat.format(ResourceBundleUtil._default().
                        getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), chaveIgreja)).
                setState(chaveIgreja).build();
    }

    public Credential saveCredentialsYouTube(String chaveIgreja, String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(chaveIgreja, YOUTUBE_SCOPES).build();
        
        TokenResponse resp = flow.newTokenRequest(code).
                setRedirectUri(MessageFormat.format(ResourceBundleUtil._default().
                        getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), chaveIgreja)).execute();

        return flow.createAndStoreCredential(resp, chaveIgreja);
    }

    private synchronized Credential loadCredentialsYouTube(String chaveIgreja) throws IOException {
        Credential credential = flow(chaveIgreja, YOUTUBE_SCOPES).build().loadCredential(chaveIgreja);

        if (credential.getExpiresInSeconds() < 15){
            credential.refreshToken();
        }

        return credential;
    }
    
    public String buscaIdCanal(String chaveIgreja) throws IOException {
        ChannelListResponse response = connect(chaveIgreja).channels().list("id").setMine(true).execute();
        
        if (!response.isEmpty()){
            return response.getItems().get(0).getId();
        }
        
        return null;
    }
    
    private YouTube connect(String igreja) throws IOException {
        return new YouTube.Builder(new ApacheHttpTransport(), new JacksonFactory(), 
                loadCredentialsYouTube(igreja)).setApplicationName("Pocket Church").build();
    }
    
    public List<VideoDTO> buscaVideos(String chave) throws IOException {
        String channelId = paramService.get(chave, TipoParametro.YOUTUBE_CHANNEL_ID);
        
        if (StringUtil.isEmpty(channelId)){
            return Collections.emptyList();
        }

        List<VideoDTO> videos = new ArrayList<VideoDTO>();

        try {
            YouTube connection = connect(chave);
            SearchListResponse response = connection.search().list("id,snippet").
                    setChannelId(channelId).
                    setMaxResults(30L).setType("video").setOrder("date").execute();

            for (SearchResult result : response.getItems()){
                VideoDTO video = new VideoDTO(
                        result.getId().getVideoId(),
                        result.getSnippet().getTitle(),
                        result.getSnippet().getDescription(),
                        new Date(result.getSnippet().getPublishedAt().getValue()));

                video.setThumbnail(result.getSnippet().getThumbnails().getDefault().getUrl());

                switch (result.getSnippet().getLiveBroadcastContent()){
                    case "live":
                        video.setAoVivo(true);
                        video.setThumbnail(result.getSnippet().getThumbnails().getHigh().getUrl());
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
        }catch (Exception ex) {
            Logger.getLogger(GoogleService.class.getName()).log(Level.SEVERE, "Erro ao recuperar streamings do YouTube", ex);
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
