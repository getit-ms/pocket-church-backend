package br.gafs.calvinista.servidor.flickr;

import br.gafs.calvinista.dto.FiltroFotoDTO;
import br.gafs.calvinista.dto.FotoDTO;
import br.gafs.calvinista.dto.GaleriaDTO;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.calvinista.servidor.google.CacheDTO;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.string.StringUtil;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.Response;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.galleries.Gallery;
import com.flickr4java.flickr.galleries.GalleryList;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gabriel on 24/07/2018.
 */
@Stateless
public class FlickrService {

    private static final Logger LOGGER = Logger.getLogger(FlickrService.class.getName());

    public static final int ITENS_POR_PAGINA = 30;
    private static final long TIMEOUT_TOKEN = 300000;

    @EJB
    private ParametroService parametroService;

    private static final Map<String, CacheDTO<Token>> CACHE_TOKEN = new HashMap<>();

    @Schedule(minute = "*/15", hour = "*")
    public void clearCache() {
        List<String> expired = new ArrayList<>();

        LOGGER.info("Realizando limpeza de cache de tokens de Flickr");
        for (Map.Entry<String, CacheDTO<Token>> entry : CACHE_TOKEN.entrySet()) {
            if (entry.getValue().isExpirado()) {
                expired.add(entry.getKey());
            }
        }

        if (expired.isEmpty()) {
            LOGGER.info("Nenhum token Filckr pra ser removido.");
        } else {
            LOGGER.info("Removendo " + expired.size() + " tokens Flickr.");

            for (String token : expired) {
                CACHE_TOKEN.remove(token);
            }

            LOGGER.info("Remoção de tokens concluída.");
        }
    }

    public BuscaPaginadaDTO<GaleriaDTO> buscaGaleriaFotos(String chaveIgreja, Integer pagina) {
        String id = parametroService.get(chaveIgreja, TipoParametro.FLICKR_ID);

        if (!StringUtil.isEmpty(id)) {

            try {
                Flickr f = getFlickr(chaveIgreja);

                GalleryList<Gallery> galeriasFlickr = doGetList(f, id, ITENS_POR_PAGINA, pagina);

                List<GaleriaDTO> galerias = new ArrayList<>();

                for (Gallery g : galeriasFlickr) {
                    galerias.add(GaleriaDTO.builder()
                            .descricao(g.getDesc())
                            .fotoPrimaria(FotoDTO.builder()
                                    .id(g.getPrimaryPhotoId())
                                    .farm(g.getPrimaryPhotoFarm())
                                    .secret(g.getPrimaryPhotoSecret())
                                    .server(g.getPrimaryPhotoServer())
                                    .build())
                            .id(g.getId())
                            .nome(g.getTitle())
                            .quantidadeFotos(Integer.parseInt(g.getPhotoCount()))
                            .build());
                }

                return new BuscaPaginadaDTO<>(galerias,
                        galeriasFlickr.getTotal(), galeriasFlickr.getPage(), galeriasFlickr.getPerPage());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao fazer a consulta e galerias de fotos para igreja " + chaveIgreja, ex);
            }
        }

        return new BuscaPaginadaDTO(Collections.emptyList(), 0, 1, 30);
    }

    private GalleryList<Gallery> doGetList(Flickr flickr, String userId, int perPage, int page) throws FlickrException {
        Map<String, Object> parameters = new HashMap();
        parameters.put("method", "flickr.galleries.getList");
        parameters.put("user_id", userId);
        if(perPage > 0) {
            parameters.put("per_page", String.valueOf(perPage));
        }

        if(page > 0) {
            parameters.put("page", String.valueOf(page));
        }

        Response response = flickr.getTransport().get(flickr.getTransport().getPath(), parameters, flickr.getApiKey(), flickr.getSharedSecret());
        if(response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } else {
            Element element = response.getPayload();
            GalleryList<Gallery> galleries = new GalleryList();
            galleries.setPage(element.getAttribute("page"));
            galleries.setPages(element.getAttribute("pages"));
            galleries.setPerPage(element.getAttribute("per_page"));
            galleries.setTotal(element.getAttribute("total"));
            NodeList galleryNodes = element.getElementsByTagName("gallery");

            for(int i = 0; i < galleryNodes.getLength(); ++i) {
                Element galleryElement = (Element)galleryNodes.item(i);
                Gallery gallery = new Gallery();
                gallery.setId(galleryElement.getAttribute("id"));
                gallery.setUrl(galleryElement.getAttribute("url"));
                User owner = new User();
                owner.setId(galleryElement.getAttribute("owner"));
                gallery.setOwner(owner);
                gallery.setCreateDate(galleryElement.getAttribute("date_create"));
                gallery.setUpdateDate(galleryElement.getAttribute("date_update"));
                gallery.setPrimaryPhotoId(galleryElement.getAttribute("primary_photo_id"));
                gallery.setPrimaryPhotoServer(galleryElement.getAttribute("primary_photo_server"));
                gallery.setPrimaryPhotoFarm(galleryElement.getAttribute("primary_photo_farm"));
                gallery.setPrimaryPhotoSecret(galleryElement.getAttribute("primary_photo_secret"));
                gallery.setPhotoCount(galleryElement.getAttribute("count_photos"));
                gallery.setVideoCount(galleryElement.getAttribute("count_videos"));
                galleries.add(gallery);
            }

            return galleries;
        }
    }

    public BuscaPaginadaDTO<FotoDTO> buscaFotos(String chaveIgreja, FiltroFotoDTO filtro) {
        String id = parametroService.get(chaveIgreja, TipoParametro.FLICKR_ID);

        if (!StringUtil.isEmpty(id)) {

            Flickr f = getFlickr(chaveIgreja);

            try {
                PhotoList<Photo> photosFlickr = (PhotoList<Photo>) f.getGalleriesInterface()
                        .getPhotos(filtro.getGaleria(), Collections.EMPTY_SET, ITENS_POR_PAGINA, filtro.getPagina());

                List<FotoDTO> fotos = new ArrayList<>();

                for (Photo p : photosFlickr) {
                    fotos.add(FotoDTO.builder()
                            .id(p.getId())
                            .titulo(p.getTitle())
                            .server(p.getServer())
                            .secret(p.getSecret())
                            .farm(p.getFarm())
                            .build());
                }

                return new BuscaPaginadaDTO<>(fotos,
                        photosFlickr.getTotal(), photosFlickr.getPage(), photosFlickr.getPerPage());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao fazer a consulta de fotos para gaeria " + filtro.getGaleria() + " da igreja " + chaveIgreja, ex);
            }
        }

        return new BuscaPaginadaDTO(Collections.emptyList(), 0, 1, 30);
    }

    private Flickr getFlickr(String chaveIgreja) {
        String apiKey = parametroService.get(chaveIgreja, TipoParametro.FLICKR_OAUTH_CLIENT_KEY);
        String sharedSecret = parametroService.get(chaveIgreja, TipoParametro.FLICKR_OAUTH_SECRET_KEY);
        return new Flickr(apiKey, sharedSecret, new REST());
    }

    public String buscaURLAutenticacaoFlickr(String chaveIgreja, String callbackURL) {
        Flickr f = getFlickr(chaveIgreja);

        Token reqToken = f.getAuthInterface().getRequestToken(callbackURL);

        CACHE_TOKEN.put(reqToken.getToken(), new CacheDTO<Token>(reqToken, System.currentTimeMillis() + TIMEOUT_TOKEN));

        return f.getAuthInterface().getAuthorizationUrl(reqToken, Permission.READ);
    }

    public void iniciaConfiguracaoFlickr(String chaveIgreja, String token, String verifier) {
        Flickr f = getFlickr(chaveIgreja);

        CacheDTO<Token> cacheToken = CACHE_TOKEN.get(token);

        if (cacheToken == null || cacheToken.isExpirado()) {
            throw new ServiceException("mensagens.MSG-052");
        }

        CACHE_TOKEN.remove(token);

        Token accessToken = f.getAuthInterface().getAccessToken(cacheToken.getDados(), new Verifier(verifier));

        try {
            parametroService.set(chaveIgreja, TipoParametro.FLICKR_ID,
                    f.getAuthInterface().checkToken(accessToken).getUser().getId());
        } catch (FlickrException e) {
            throw new ServiceException("mensagens.MSG-052", e);
        }
    }

    public void devinculaFlickr(String chaveIgreja) {
        parametroService.set(chaveIgreja, TipoParametro.FLICKR_ID, null);
    }
}
