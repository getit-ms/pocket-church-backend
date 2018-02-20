/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.servidor.google;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.BuscaPaginadaEventosCalendarioDTO;
import br.gafs.calvinista.dto.EventoCalendarioDTO;
import br.gafs.calvinista.dto.VideoDTO;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.util.string.StringUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.*;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.calendar.Calendar;

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
    
    public static List<String> GOOGLE_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("GOOGLE_SCOPES").split("\\s*,\\s*"));

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
    
    public String getURLAutorizacao(String chaveIgreja, String urlCallback) throws IOException {
        return flow(chaveIgreja, GOOGLE_SCOPES).setApprovalPrompt("force").build().newAuthorizationUrl().
                setRedirectUri(MessageFormat.format(urlCallback, chaveIgreja)).
                setState(chaveIgreja).build();
    }

    public Credential saveCredentials(String chaveIgreja, String callbackURL, String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(chaveIgreja, GOOGLE_SCOPES).build();
        
        TokenResponse resp = flow.newTokenRequest(code).
                setRedirectUri(MessageFormat.format(callbackURL, chaveIgreja)).execute();

        return flow.createAndStoreCredential(resp, chaveIgreja);
    }

    private synchronized Credential loadCredentials(String chaveIgreja) throws IOException {
        Credential credential = flow(chaveIgreja, GOOGLE_SCOPES).build().loadCredential(chaveIgreja);

        if (credential.getExpiresInSeconds() < 15){
            credential.refreshToken();
        }

        return credential;
    }

    public String buscaIdCalendar(String chaveIgreja) throws IOException {
        CalendarList response = connectCalendar(chaveIgreja).calendarList().list().execute();

        if (!response.isEmpty()) {
            return response.getItems().get(0).getId();
        }

        return null;
    }
    
    public String buscaIdCanalYouTube(String chaveIgreja) throws IOException {
        ChannelListResponse response = connectYouTube(chaveIgreja).channels().list("id").setMine(true).execute();
        
        if (!response.isEmpty()){
            return response.getItems().get(0).getId();
        }
        
        return null;
    }

    private Calendar connectCalendar(String igreja) throws IOException {
        return new Calendar.Builder(new ApacheHttpTransport(), new JacksonFactory(),
                loadCredentials(igreja)).setApplicationName("Pocket Church").build();
    }

    private YouTube connectYouTube(String igreja) throws IOException {
        return new YouTube.Builder(new ApacheHttpTransport(), new JacksonFactory(), 
                loadCredentials(igreja)).setApplicationName("Pocket Church").build();

    }
    public BuscaPaginadaEventosCalendarioDTO buscaEventosCalendar(String chave, String pageToken, Integer tamanho) throws IOException {
        String calendarId = paramService.get(chave, TipoParametro.GOOGLE_CALENDAR_ID);

        List<EventoCalendarioDTO> eventos = new ArrayList<EventoCalendarioDTO>();

        try {
            Events response = connectCalendar(chave).events().list(calendarId)
                    .setTimeMin(new DateTime(new Date())).setMaxResults(tamanho + 1)
                    .setPageToken(pageToken).setSingleEvents(true).execute();

            for (Event event : response.getItems()) {
                EventoCalendarioDTO evento = new EventoCalendarioDTO();

                evento.setId(event.getId());
                evento.setInicio(new Date(event.getStart().getDateTime().getValue()));
                evento.setTermino(new Date(event.getEnd().getDateTime().getValue()));
                evento.setDescricao(event.getSummary());
                evento.setLocal(event.getLocation());

                eventos.add(evento);
            }

            return new BuscaPaginadaEventosCalendarioDTO(eventos, response.getNextPageToken());
        } catch (Exception ex) {
            Logger.getLogger(GoogleService.class.getName()).log(Level.SEVERE, "Erro ao recuperar eventos do Google Calendar", ex);
        }

        return new BuscaPaginadaEventosCalendarioDTO(eventos, null);
    }
    
    public List<VideoDTO> buscaVideosYouTube(String chave) throws IOException {
        String channelId = paramService.get(chave, TipoParametro.YOUTUBE_CHANNEL_ID);
        
        if (StringUtil.isEmpty(channelId)){
            return Collections.emptyList();
        }

        List<VideoDTO> videos = new ArrayList<VideoDTO>();

        try {
            YouTube connection = connectYouTube(chave);
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

                        if (!liveResponse.isEmpty() && !liveResponse.getItems().isEmpty()){
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

    public List<VideoDTO> buscaStreamsAtivosYouTube(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();
        
        for (VideoDTO video : buscaVideosYouTube(chave)){
            if (video.isAoVivo()){
                videos.add(video);
            }
        }
        
        return videos;
    }

    public List<VideoDTO> buscaStreamsAgendadosYouTube(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();
        
        for (VideoDTO video : buscaVideosYouTube(chave)){
            if (video.isAgendado()){
                videos.add(video);
            }
        }
        
        return videos;
    }
    
}
