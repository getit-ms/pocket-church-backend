/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.servidor.google;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.pocket.corporate.dto.BuscaPaginadaEventosCalendarioDTO;
import br.gafs.pocket.corporate.dto.CalendarioGoogleDTO;
import br.gafs.pocket.corporate.dto.EventoCalendarioDTO;
import br.gafs.pocket.corporate.dto.VideoDTO;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.util.date.DateUtil;
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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Gabriel
 */
@Stateless
public class GoogleService {

    private static final long MILLIS_MINUTO = 60000;
    public static List<String> YOUTUBE_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("YOUTUBE_SCOPES").split("\\s*,\\s*"));

    public static List<String> GOOGLE_CALENDAR_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("GOOGLE_CALENDAR_SCOPES").split("\\s*,\\s*"));

    private static final File GOOGLE_STORE_DIR = new File(ResourceBundleUtil._default().getPropriedade("GOOGLE_STORE_DIR"));
    
    private static Map<String, FileDataStoreFactory> DATA_STORE_FACTORY = new HashMap<>();

    private static final Map<String, CacheDTO<List<VideoDTO>>> CACHE_VIDEOS = new HashMap<>();

    private static final String GOOGLE_CALENDAR_CHAVE = "CAL";
    private static final String YOUTUBE_CHAVE = "YTB";

    public static final Logger LOGGER = Logger.getLogger(GoogleService.class.getName());

    static {
        if (!GOOGLE_STORE_DIR.exists()){
            GOOGLE_STORE_DIR.mkdirs();
        }
        try {
            DATA_STORE_FACTORY.put(GOOGLE_CALENDAR_CHAVE, new FileDataStoreFactory(new File(GOOGLE_STORE_DIR, GOOGLE_CALENDAR_CHAVE + "store")));
            DATA_STORE_FACTORY.put(YOUTUBE_CHAVE, new FileDataStoreFactory(new File(GOOGLE_STORE_DIR, YOUTUBE_CHAVE + "store")));
        } catch (IOException ex) {
            Logger.getLogger(GoogleService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @EJB
    private ParametroService paramService;

    private GoogleAuthorizationCodeFlow.Builder flow(String store, String chaveEmpresa, Collection<String> scopes) throws IOException{
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                (String) paramService.get(chaveEmpresa, TipoParametro.GOOGLE_OAUTH_CLIENT_KEY),
                (String) paramService.get(chaveEmpresa, TipoParametro.GOOGLE_OAUTH_SECRET_KEY),
                scopes).addRefreshListener(new DataStoreCredentialRefreshListener(chaveEmpresa, DATA_STORE_FACTORY.get(store))).
                setDataStoreFactory(DATA_STORE_FACTORY.get(store)).setAccessType("offline");
    }
    
    public String getURLAutorizacaoYouTube(String chaveEmpresa, String urlCallback) throws IOException {
        return getURLAutorizacao(YOUTUBE_CHAVE, chaveEmpresa, YOUTUBE_SCOPES, urlCallback);
    }

    public String getURLAutorizacaoCalendar(String chaveEmpresa, String urlCallback) throws IOException {
        return getURLAutorizacao(GOOGLE_CALENDAR_CHAVE, chaveEmpresa, GOOGLE_CALENDAR_SCOPES, urlCallback);
    }

    private String getURLAutorizacao(String store, String chaveEmpresa, Collection<String> scopes, String urlCallback) throws IOException {
        return flow(store, chaveEmpresa, scopes).setApprovalPrompt("force").build().newAuthorizationUrl().
                setRedirectUri(MessageFormat.format(urlCallback, chaveEmpresa)).
                setState(chaveEmpresa).build();
    }

    public Credential saveCredentialsYouTube(String chaveEmpresa, String callbackURL, String code) throws IOException {
        return saveCredentials(YOUTUBE_CHAVE, chaveEmpresa, YOUTUBE_SCOPES, callbackURL, code);
    }

    public Credential saveCredentialsGoogleCalendar(String chaveEmpresa, String callbackURL, String code) throws IOException {
        return saveCredentials(GOOGLE_CALENDAR_CHAVE, chaveEmpresa, GOOGLE_CALENDAR_SCOPES, callbackURL, code);
    }

    private Credential saveCredentials(String store, String chaveEmpresa, Collection<String> scopes, String callbackURL, String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(store, chaveEmpresa, scopes).build();
        
        TokenResponse resp = flow.newTokenRequest(code).
                setRedirectUri(MessageFormat.format(callbackURL, chaveEmpresa)).execute();

        return flow.createAndStoreCredential(resp, chaveEmpresa);
    }

    private Credential loadCredentials(String store, String chaveEmpresa, Collection<String> scopes) throws IOException {
        Credential credential = flow(store, chaveEmpresa, scopes).build().loadCredential(chaveEmpresa);

        if (credential.getExpiresInSeconds() < 15){
            credential.refreshToken();
        }

        return credential;
    }

    public List<String> buscaIdsCalendar(String chaveEmpresa) throws IOException {
        CalendarList response = connectCalendar(chaveEmpresa).calendarList().list().execute();

        if (!response.isEmpty()) {
            List<String> ids = new ArrayList<>();
            for (CalendarListEntry entry : response.getItems()) {
                ids.add(entry.getId());
            }
            return ids;
        }

        return Collections.emptyList();
    }
    
    public String buscaIdCanalYouTube(String chaveEmpresa) throws IOException {
        ChannelListResponse response = connectYouTube(chaveEmpresa).channels().list("id").setMine(true).execute();
        
        if (!response.isEmpty()){
            return response.getItems().get(0).getId();
        }
        
        return null;
    }

    private Calendar connectCalendar(String empresa) throws IOException {
        return new Calendar.Builder(new ApacheHttpTransport(), new JacksonFactory(),
                loadCredentials(GOOGLE_CALENDAR_CHAVE, empresa, GOOGLE_CALENDAR_SCOPES))
                .setApplicationName("Pocket Corporate").build();
    }

    private YouTube connectYouTube(String empresa) throws IOException {
        return new YouTube.Builder(new ApacheHttpTransport(), new JacksonFactory(),
                loadCredentials(YOUTUBE_CHAVE, empresa, YOUTUBE_SCOPES))
                .setApplicationName("Pocket Corporate").build();

    }

    public List<CalendarioGoogleDTO> buscaCalendarios(String chaveEmpresa) throws IOException {
        if (paramService.get(chaveEmpresa, TipoParametro.GOOGLE_CALENDAR_ID) != null) {
            CalendarList response = connectCalendar(chaveEmpresa).calendarList().list().execute();

            if (!response.isEmpty()) {
                List<CalendarioGoogleDTO> calendarios = new ArrayList<>();
                for (CalendarListEntry entry : response.getItems()) {
                    calendarios.add(new CalendarioGoogleDTO(entry.getId(), entry.getSummary()));
                }
                return calendarios;
            }
        }

        return Collections.emptyList();
    }

    public BuscaPaginadaEventosCalendarioDTO buscaEventosCalendar(String chave, String calendarId,
                                                                  String pageToken, Integer tamanho) throws IOException {
        List<EventoCalendarioDTO> eventos = new ArrayList<EventoCalendarioDTO>();

        try {
            Events response = connectCalendar(chave).events().list(calendarId)
                    .setTimeMin(new DateTime(new Date()))
                    .setMaxResults(tamanho + 1).setShowHiddenInvitations(true)
                    .setPageToken(pageToken)
                    .setSingleEvents(true).setOrderBy("startTime").execute();

            for (Event event : response.getItems()) {
                if (event.getStart() != null && event.getEnd() != null) {
                    EventoCalendarioDTO evento = new EventoCalendarioDTO();

                    evento.setId(event.getId());

                    if (event.getStart().getDateTime() != null) {
                        evento.setInicio(new Date(event.getStart().getDateTime().getValue()));
                    } else {
                        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                        cal.setTimeInMillis(event.getStart().getDate().getValue());

                        evento.setInicio(DateUtil.criarDataAtualSemHora(
                                cal.get(java.util.Calendar.DAY_OF_MONTH),
                                cal.get(java.util.Calendar.MONTH) + 1,
                                cal.get(java.util.Calendar.YEAR)
                        ));
                    }

                    if (event.getEnd().getDateTime() != null) {
                        evento.setTermino(new Date(event.getEnd().getDateTime().getValue()));
                    } else {
                        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                        cal.setTimeInMillis(event.getEnd().getDate().getValue());

                        evento.setTermino(DateUtil.criarDataAtualSemHora(
                                cal.get(java.util.Calendar.DAY_OF_MONTH),
                                cal.get(java.util.Calendar.MONTH) + 1,
                                cal.get(java.util.Calendar.YEAR)
                        ));
                    }

                    evento.setDescricao(event.getSummary());
                    evento.setLocal(event.getLocation());

                    eventos.add(evento);
                }
            }

            return new BuscaPaginadaEventosCalendarioDTO(eventos, response.getNextPageToken());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao recuperar eventos do Google Calendar", ex);
        }

        return new BuscaPaginadaEventosCalendarioDTO(eventos, null);
    }

    public List<VideoDTO> buscaVideosYouTube(String chave) throws IOException {
        return buscaVideosYouTube(chave, false);
    }

    public List<VideoDTO> buscaVideosYouTube(String chave, boolean force) throws IOException {
        String channelId = paramService.get(chave, TipoParametro.YOUTUBE_CHANNEL_ID);

        if (StringUtil.isEmpty(channelId)){
            return Collections.emptyList();
        }

        List<VideoDTO> videos = new ArrayList<VideoDTO>();

        CacheDTO<List<VideoDTO>> cache = CACHE_VIDEOS.get(chave);

        if (force || cache == null || cache.isExpirado()) {

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

            CACHE_VIDEOS.put(chave, new CacheDTO(videos, System.currentTimeMillis() + MILLIS_MINUTO));
        } else {
            videos.addAll(cache.getDados());
        }

        return videos;
    }

    public List<VideoDTO> buscaStreamsAtivosYouTube(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();
        
        for (VideoDTO video : buscaVideosYouTube(chave, true)){
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
