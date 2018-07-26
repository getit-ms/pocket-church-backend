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
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.galleries.Gallery;
import com.flickr4java.flickr.galleries.GalleryList;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

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
                Flickr f = getFlickr(id, (String) parametroService.get(chaveIgreja, TipoParametro.FLICKR_OAUTH_CLIENT_KEY));

                GalleryList<Gallery> galeriasFlickr = (GalleryList<Gallery>) f.getGalleriesInterface().getList(id, ITENS_POR_PAGINA, pagina);

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

    public BuscaPaginadaDTO<FotoDTO> buscaFotos(String chaveIgreja, FiltroFotoDTO filtro) {
        String id = parametroService.get(chaveIgreja, TipoParametro.FLICKR_ID);

        if (!StringUtil.isEmpty(id)) {

            Flickr f = getFlickr(id, (String) parametroService.get(chaveIgreja, TipoParametro.FLICKR_OAUTH_CLIENT_KEY));

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
        return getFlickr(
                (String) parametroService.get(chaveIgreja, TipoParametro.FLICKR_OAUTH_CLIENT_KEY),
                (String) parametroService.get(chaveIgreja, TipoParametro.FLICKR_OAUTH_SECRET_KEY)
        );
    }

    private Flickr getFlickr(String apiKey, String sharedSecret) {
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
            Auth auth = f.getAuthInterface().checkToken(accessToken);

            parametroService.set(chaveIgreja, TipoParametro.FLICKR_ID, auth.getToken());
        } catch (FlickrException e) {
            throw new ServiceException("mensagens.MSG-052", e);
        }
    }

    public void devinculaFlickr(String chaveIgreja) {
        parametroService.set(chaveIgreja, TipoParametro.FLICKR_ID, null);
    }
}
