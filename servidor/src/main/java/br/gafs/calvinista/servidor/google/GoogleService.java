/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor.google;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.VideoDTO;
import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.SessaoBean;
import br.gafs.util.string.StringUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author Gabriel
 */
@Stateless
public class GoogleService {
    
    @Inject
    private SessaoBean sessao;
    
    public static List<String> YOUTUBE_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("YOUTUBE_SCOPES").split("\\s*,\\s*"));
    
    @EJB
    private ParametroService paramService;
    
    private GoogleAuthorizationCodeFlow flow(Collection<String> scopes){
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                (String) paramService.get(sessao.getChaveIgreja(), TipoParametro.GOOGLE_OAUTH_CLIENT_KEY), 
                (String) paramService.get(sessao.getChaveIgreja(), TipoParametro.GOOGLE_OAUTH_SECRET_KEY),
                scopes).setAccessType("offline").build();
    }
    
    public String getURLAutorizacaoYouTube(){
        return flow(YOUTUBE_SCOPES).newAuthorizationUrl().
                setRedirectUri(MessageFormat.format(ResourceBundleUtil._default().
                        getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), sessao.getChaveIgreja())).
                setState(sessao.getChaveIgreja()).build();
    }
    
    public Credential saveCredentialsYouTube(String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(YOUTUBE_SCOPES);
        TokenResponse resp = flow.newTokenRequest(code).
                setRedirectUri(MessageFormat.format(ResourceBundleUtil._default().
                        getPropriedade("OAUTH_YOUTUBE_REDIRECT_URL"), sessao.getChaveIgreja())).execute();
        return flow.createAndStoreCredential(resp, sessao.getChaveIgreja());
    }
    
    public Credential loadCredentialsYouTube() throws IOException {
        return loadCredentialsYouTube(sessao.getChaveIgreja());
    }
    
    private Credential loadCredentialsYouTube(String chaveIgreja) throws IOException {
        return flow(YOUTUBE_SCOPES).loadCredential(chaveIgreja);
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
                loadCredentialsYouTube(igreja)).build();
    }
    
    public List<VideoDTO> buscaVideos() throws IOException {
        String channelId = (String) paramService.get(sessao.getChaveIgreja(), TipoParametro.YOUTUBE_CHANNEL_ID);
        
        if (StringUtil.isEmpty(channelId)){
            return Collections.emptyList();
        }
        
        YouTube connection = connect(sessao.getChaveIgreja());
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
        }
        
        return videos;
    }
    
}
