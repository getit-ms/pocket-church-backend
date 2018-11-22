package br.gafs.calvinista.servidor.facebook;

import br.gafs.bundle.ResourceBundleUtil;
import br.gafs.calvinista.dto.PaginaFacebookDTO;
import br.gafs.calvinista.dto.VideoDTO;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.google.CacheDTO;
import br.gafs.calvinista.servidor.google.GoogleService;
import br.gafs.util.string.StringUtil;
import com.restfb.*;
import com.restfb.json.JsonObject;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 16/11/2018.
 */
@Stateless
public class FacebookService {

    private static final String CALLBACK_URL = ResourceBundleUtil._default().getPropriedade("OAUTH_FACEBOOK_VIDEO_REDIRECT_URL");

    private static final Map<String, CacheDTO<List<VideoDTO>>> CACHE_VIDEOS = new HashMap<>();
    private static final long MILLIS_MINUTO = 60000;

    @EJB
    private ParametroService paramService;

    private final DefaultFacebookClient client = new DefaultFacebookClient(Version.VERSION_2_12);

    public String getLoginUrlVideos(String chave) {
        ScopeBuilder scopeBuilder = new ScopeBuilder();
        scopeBuilder.addPermission(FacebookPermissions.PAGES_SHOW_LIST);
        scopeBuilder.addPermission(FacebookPermissions.MANAGE_PAGES);
        return client.getLoginDialogUrl(
                (String) paramService.get(chave, TipoParametro.FACEBOOK_APP_ID),
                MessageFormat.format(CALLBACK_URL, chave), scopeBuilder,
                Parameter.with("auth_type", "true"));
    }

    public void login(String chave, String code) {
        paramService.set(chave, TipoParametro.FACEBOOK_APP_CODE, code);
    }

    public List<PaginaFacebookDTO> buscaPaginas(String chave) {
        FacebookClient.AccessToken accessToken = getAccessToken(chave);

        Connection<PaginaFacebookDTO> connection = client.fetchConnection("/me/accounts", PaginaFacebookDTO.class,
                Parameter.with("access_token", accessToken.getAccessToken()));

        return connection.getData();
    }

    public void logout(String chave) {
        paramService.set(chave, TipoParametro.FACEBOOK_APP_CODE, null);
    }

    private FacebookClient.AccessToken getAccessToken(String chave) {
        return client.obtainUserAccessToken(
                (String) paramService.get(chave, TipoParametro.FACEBOOK_APP_ID),
                (String) paramService.get(chave, TipoParametro.FACEBOOK_APP_SECRET),
                MessageFormat.format(CALLBACK_URL, chave),
                (String) paramService.get(chave, TipoParametro.FACEBOOK_APP_CODE)
        );
    }

    public List<VideoDTO> buscaVideos(String chave) throws IOException {
        return buscaVideos(chave, false);
    }

    public List<VideoDTO> buscaVideos(String chave, boolean force) throws IOException {
        String code = paramService.get(chave, TipoParametro.FACEBOOK_APP_CODE);
        String pageId = paramService.get(chave, TipoParametro.FACEBOOK_PAGE_ID);

        if (StringUtil.isEmpty(code) || StringUtil.isEmpty(pageId)){
            return Collections.emptyList();
        }

        List<VideoDTO> videos = new ArrayList<VideoDTO>();

        CacheDTO<List<VideoDTO>> cache = CACHE_VIDEOS.get(chave);

        if (force || cache == null || cache.isExpirado()) {

            try {
                FacebookClient.AccessToken accessToken = getAccessToken(chave);

                Connection<JsonObject> connection = client.fetchConnection("/" + pageId + "/live_videos", JsonObject.class,
                        Parameter.with("access_token", accessToken.getAccessToken()),
                        Parameter.with("fields", "dash_preview_url,creation_time,broadcast_start_time,secure_stream_url,status,title"));

                for (JsonObject result : connection.getData()){
                    VideoDTO video = new VideoDTO(
                            result.getString("id", null),
                            result.getString("title", null),
                            null,
                            new Date(result.getLong("creation_time", System.currentTimeMillis()))
                            );

                    video.setThumbnail(result.getString("dash_preview_url", null));
                    video.setStreamUrl(result.getString("secure_stream_url", null));

                    switch (result.getString("status", "")){
                        case "LIVE_NOW":
                            video.setAoVivo(true);
                            break;
                        case "SCHEDULED_LIVE":
                        case "SCHEDULED_UNPUBLISHED":
                            if (result.get("broadcast_start_time").asString() != null){
                                video.setAgendamento(new Date(result.get("broadcast_start_time").asLong()));
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

    public List<VideoDTO> buscaStreamsAtivos(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();

        for (VideoDTO video : buscaVideos(chave, true)){
            if (video.isAoVivo()){
                videos.add(video);
            }
        }

        return videos;
    }

    public List<VideoDTO> buscaStreamsAgendados(String chave) throws IOException {
        List<VideoDTO> videos = new ArrayList<VideoDTO>();

        for (VideoDTO video : buscaVideos(chave)){
            if (video.isAgendado()){
                videos.add(video);
            }
        }

        return videos;
    }
}