/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.servidor.google;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.BuscaPaginadaEventosCalendarioDTO;
import br.gafs.calvinista.dto.CalendarioGoogleDTO;
import br.gafs.calvinista.dto.EventoCalendarioDTO;
import br.gafs.calvinista.entity.Video;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
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

/**
 * @author Gabriel
 */
@Stateless
public class GoogleService {

    public static List<String> YOUTUBE_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("YOUTUBE_SCOPES").split("\\s*,\\s*"));

    public static List<String> GOOGLE_CALENDAR_SCOPES = Arrays.asList(ResourceBundleUtil.
            _default().getPropriedade("GOOGLE_CALENDAR_SCOPES").split("\\s*,\\s*"));

    private static final File GOOGLE_STORE_DIR = new File(ResourceBundleUtil._default().getPropriedade("GOOGLE_STORE_DIR"));

    private static Map<String, FileDataStoreFactory> DATA_STORE_FACTORY = new HashMap<>();

    private static final String GOOGLE_CALENDAR_CHAVE = "CAL";
    private static final String YOUTUBE_CHAVE = "YTB";

    public static final Logger LOGGER = Logger.getLogger(GoogleService.class.getName());

    static {
        if (!GOOGLE_STORE_DIR.exists()) {
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

    private GoogleAuthorizationCodeFlow.Builder flow(String store, String chaveIgreja, Collection<String> scopes) throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                (String) paramService.get(chaveIgreja, TipoParametro.GOOGLE_OAUTH_CLIENT_KEY),
                (String) paramService.get(chaveIgreja, TipoParametro.GOOGLE_OAUTH_SECRET_KEY),
                scopes).addRefreshListener(new DataStoreCredentialRefreshListener(chaveIgreja, DATA_STORE_FACTORY.get(store))).
                setDataStoreFactory(DATA_STORE_FACTORY.get(store)).setAccessType("offline");
    }

    public String getURLAutorizacaoYouTube(String chaveIgreja, String urlCallback) throws IOException {
        return getURLAutorizacao(YOUTUBE_CHAVE, chaveIgreja, YOUTUBE_SCOPES, urlCallback);
    }

    public String getURLAutorizacaoCalendar(String chaveIgreja, String urlCallback) throws IOException {
        return getURLAutorizacao(GOOGLE_CALENDAR_CHAVE, chaveIgreja, GOOGLE_CALENDAR_SCOPES, urlCallback);
    }

    private String getURLAutorizacao(String store, String chaveIgreja, Collection<String> scopes, String urlCallback) throws IOException {
        return flow(store, chaveIgreja, scopes).setApprovalPrompt("force").build().newAuthorizationUrl().
                setRedirectUri(MessageFormat.format(urlCallback, chaveIgreja)).
                setState(chaveIgreja).build();
    }

    public Credential saveCredentialsYouTube(String chaveIgreja, String callbackURL, String code) throws IOException {
        return saveCredentials(YOUTUBE_CHAVE, chaveIgreja, YOUTUBE_SCOPES, callbackURL, code);
    }

    public Credential saveCredentialsGoogleCalendar(String chaveIgreja, String callbackURL, String code) throws IOException {
        return saveCredentials(GOOGLE_CALENDAR_CHAVE, chaveIgreja, GOOGLE_CALENDAR_SCOPES, callbackURL, code);
    }

    private Credential saveCredentials(String store, String chaveIgreja, Collection<String> scopes, String callbackURL, String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(store, chaveIgreja, scopes).build();

        TokenResponse resp = flow.newTokenRequest(code).
                setRedirectUri(MessageFormat.format(callbackURL, chaveIgreja)).execute();

        return flow.createAndStoreCredential(resp, chaveIgreja);
    }

    private Credential loadCredentials(String store, String chaveIgreja, Collection<String> scopes) throws IOException {
        Credential credential = flow(store, chaveIgreja, scopes).build().loadCredential(chaveIgreja);

        if (credential != null && credential.getExpiresInSeconds() < 15) {
            credential.refreshToken();
        }

        return credential;
    }

    public List<String> buscaIdsCalendar(String chaveIgreja) throws IOException {
        CalendarList response = connectCalendar(chaveIgreja).calendarList().list().execute();

        if (!response.isEmpty()) {
            List<String> ids = new ArrayList<>();
            for (CalendarListEntry entry : response.getItems()) {
                ids.add(entry.getId());
            }
            return ids;
        }

        return Collections.emptyList();
    }

    public String buscaIdCanalYouTube(String chaveIgreja) throws IOException {
        ChannelListResponse response = connectYouTube(chaveIgreja).channels().list("id").setMine(true).execute();

        if (!response.isEmpty()) {
            return response.getItems().get(0).getId();
        }

        return null;
    }

    private Calendar connectCalendar(String igreja) throws IOException {
        return new Calendar.Builder(new ApacheHttpTransport(), new JacksonFactory(),
                loadCredentials(GOOGLE_CALENDAR_CHAVE, igreja, GOOGLE_CALENDAR_SCOPES))
                .setApplicationName("Pocket Church").build();
    }

    private YouTube connectYouTube(String igreja) throws IOException {
        return new YouTube.Builder(new ApacheHttpTransport(), new JacksonFactory(),
                loadCredentials(YOUTUBE_CHAVE, igreja, YOUTUBE_SCOPES))
                .setApplicationName("Pocket Church").build();

    }

    public List<CalendarioGoogleDTO> buscaCalendarios(String chaveIgreja) throws IOException {
        if (paramService.get(chaveIgreja, TipoParametro.GOOGLE_CALENDAR_ID) != null) {
            CalendarList response = connectCalendar(chaveIgreja).calendarList().list().execute();

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

    public List<Video> buscaVideosYouTube(String chave) throws IOException {
        return buscaVideosYouTube(chave, false);
    }

    public List<Video> buscaVideosYouTube(String chave, boolean force) throws IOException {
        String channelId = paramService.get(chave, TipoParametro.YOUTUBE_CHANNEL_ID);

        if (StringUtil.isEmpty(channelId)) {
            return Collections.emptyList();
        }

        List<Video> videos = new ArrayList<>();

        try {
            YouTube connection = connectYouTube(chave);

            try {
                buscaLives(channelId, videos, connection);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Falha ao recuperar as lives do canal", ex);
            }

            buscaHistoricoVideos(channelId, videos, connection);

            Collections.sort(videos, new Comparator<Video>() {
                @Override
                public int compare(Video o1, Video o2) {
                    return o2.getPublicacao().compareTo(o1.getPublicacao());
                }
            });

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao recuperar streamings do YouTube", ex);
        }


        return videos;
    }

    private void buscaHistoricoVideos(String channelId, List<Video> videos, YouTube connection) throws IOException {
        List<Channel> channels = connection.channels().list("id,contentDetails").
                setId(channelId).execute().getItems();

        PlaylistItemListResponse response = connection.playlistItems().list("id,snippet,status")
                .setPlaylistId(channels.get(0).getContentDetails().getRelatedPlaylists().getUploads())
                .setMaxResults(30L).execute();

        for (PlaylistItem result : response.getItems()) {
            if (result.getSnippet().getResourceId().getVideoId() == null
                    || !"public".equals(result.getStatus().getPrivacyStatus())) {
                continue;
            }

            Video video = Video.builder()
                    .id(result.getSnippet().getResourceId().getVideoId())
                    .titulo(result.getSnippet().getTitle())
                    .descricao(result.getSnippet().getDescription())
                    .publicacao(new Date(result.getSnippet().getPublishedAt().getValue()))
                    .build();

            if (result.getSnippet().getThumbnails() != null &&
                    result.getSnippet().getThumbnails().getStandard() != null) {
                video.setThumbnail(result.getSnippet().getThumbnails().getStandard().getUrl());

                if (!videos.contains(video)) {
                    videos.add(video);
                }
            }

        }
    }

    private void buscaLives(String channelId, List<Video> videos, YouTube connection) throws IOException {
        LiveBroadcastListResponse lives = connection.liveBroadcasts().list("id,snippet,status")
                .setMine(true).execute();

        for (LiveBroadcast live : lives.getItems()) {
            if (live.getSnippet().getChannelId().equals(channelId)) {
                if (!"public".equals(live.getStatus().getPrivacyStatus())) {
                    return;
                }

                Video video = Video.builder()
                        .id(live.getId())
                        .titulo(live.getSnippet().getTitle())
                        .descricao(live.getSnippet().getDescription())
                        .publicacao(new Date(live.getSnippet().getPublishedAt().getValue()))
                        .aoVivo("live".equals(live.getStatus().getLifeCycleStatus()))
                        .build();


                if (live.getSnippet().getScheduledStartTime() != null) {
                    Date scheduledStartTime = new Date(live.getSnippet().getScheduledStartTime().getValue());

                    if (scheduledStartTime.getTime() > System.currentTimeMillis()) {
                        video.setAgendamento(scheduledStartTime);
                    }
                }

                if (live.getSnippet().getThumbnails() != null &&
                        live.getSnippet().getThumbnails().getStandard() != null) {
                    video.setThumbnail(live.getSnippet().getThumbnails().getStandard().getUrl());

                    if (!videos.contains(video)) {
                        videos.add(video);
                    }
                }
            }
        }
    }

}
