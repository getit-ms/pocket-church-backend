package br.gafs.pocket.corporate.servidor.flickr;

import br.gafs.pocket.corporate.dto.FiltroFotoDTO;
import br.gafs.pocket.corporate.dto.FotoDTO;
import br.gafs.pocket.corporate.dto.GaleriaDTO;
import br.gafs.pocket.corporate.entity.domain.TipoParametro;
import br.gafs.pocket.corporate.service.ParametroService;
import br.gafs.pocket.corporate.servidor.google.CacheDTO;
import br.gafs.dao.BuscaPaginadaDTO;
import br.gafs.exceptions.ServiceException;
import br.gafs.util.string.StringUtil;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth1Token;

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

    private static final Map<String, CacheDTO<OAuth1RequestToken>> CACHE_TOKEN = new HashMap<>();

    @Schedule(minute = "*/15", hour = "*", persistent = false)
    public void clearCache() {
        List<String> expired = new ArrayList<>();

        LOGGER.info("Realizando limpeza de cache de tokens de Flickr.");
        for (Map.Entry<String, CacheDTO<OAuth1RequestToken>> entry : CACHE_TOKEN.entrySet()) {
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

    public BuscaPaginadaDTO<GaleriaDTO> buscaGaleriaFotos(String chaveEmpresa, Integer pagina) {
        String id = parametroService.get(chaveEmpresa, TipoParametro.FLICKR_ID);

        if (!StringUtil.isEmpty(id)) {

            try {
                Flickr f = getFlickr(chaveEmpresa);

                Photosets photoSets = f.getPhotosetsInterface().getList(id, ITENS_POR_PAGINA, pagina, null);

                List<GaleriaDTO> galerias = new ArrayList<>();

                for (Photoset set : photoSets.getPhotosets()) {
                    galerias.add(GaleriaDTO.builder()
                            .id(set.getId())
                            .nome(set.getTitle())
                            .descricao(set.getDescription())
                            .quantidadeFotos(set.getPhotoCount())
                            .dataAtualizacao(StringUtil.isEmpty(set.getDateUpdate()) ?
                                    new Date(Long.parseLong(set.getDateCreate())) :
                                    new Date(Long.parseLong(set.getDateUpdate())))
                            .fotoPrimaria(FotoDTO.builder()
                                    .id(set.getPrimaryPhoto().getId())
                                    .farm(set.getPrimaryPhoto().getFarm())
                                    .secret(set.getPrimaryPhoto().getSecret())
                                    .server(set.getPrimaryPhoto().getServer())
                                    .build())
                            .build());
                }

                return new BuscaPaginadaDTO<>(galerias,
                        photoSets.getTotal(), photoSets.getPage(), photoSets.getPerPage());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao fazer a consulta e galerias de fotos para empresa " + chaveEmpresa, ex);
            }
        }

        return new BuscaPaginadaDTO(Collections.emptyList(), 0, 1, 30);
    }

    public BuscaPaginadaDTO<FotoDTO> buscaFotos(String chaveEmpresa, FiltroFotoDTO filtro) {
        String id = parametroService.get(chaveEmpresa, TipoParametro.FLICKR_ID);

        if (!StringUtil.isEmpty(id)) {

            Flickr f = getFlickr(chaveEmpresa);

            try {
                LOGGER.info("Realizando consulta de fotos de galeira com o Flickr.");

                PhotoList<Photo> photosFlickr = f.getPhotosetsInterface()
                        .getPhotos(filtro.getGaleria(), ITENS_POR_PAGINA, filtro.getPagina());

                LOGGER.info("Consulta de fotos de galeira com o Flickr realizada com sucesso. Total de resultados " + photosFlickr.size());

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
                LOGGER.log(Level.SEVERE, "Erro ao fazer a consulta de fotos para gaeria " + filtro.getGaleria() + " da empresa " + chaveEmpresa, ex);
            }
        }

        return new BuscaPaginadaDTO(Collections.emptyList(), 0, 1, 30);
    }

    private Flickr getFlickr(String chaveEmpresa) {
        String apiKey = parametroService.get(chaveEmpresa, TipoParametro.FLICKR_OAUTH_CLIENT_KEY);
        String sharedSecret = parametroService.get(chaveEmpresa, TipoParametro.FLICKR_OAUTH_SECRET_KEY);
        return new Flickr(apiKey, sharedSecret, new REST());
    }

    public String buscaURLAutenticacaoFlickr(String chaveEmpresa, String callbackURL) {
        Flickr f = getFlickr(chaveEmpresa);

        OAuth1RequestToken reqToken = f.getAuthInterface().getRequestToken(callbackURL);

        CACHE_TOKEN.put(reqToken.getToken(), new CacheDTO<>(reqToken, System.currentTimeMillis() + TIMEOUT_TOKEN));

        return f.getAuthInterface().getAuthorizationUrl(reqToken, Permission.READ);
    }

    public void iniciaConfiguracaoFlickr(String chaveEmpresa, String token, String verifier) {
        Flickr f = getFlickr(chaveEmpresa);

        CacheDTO<OAuth1RequestToken> cacheToken = CACHE_TOKEN.get(token);

        if (cacheToken == null || cacheToken.isExpirado()) {
            throw new ServiceException("mensagens.MSG-052");
        }

        CACHE_TOKEN.remove(token);

        OAuth1Token accessToken = f.getAuthInterface().getAccessToken(cacheToken.getDados(), verifier);

        try {
            parametroService.set(chaveEmpresa, TipoParametro.FLICKR_ID,
                    f.getAuthInterface().checkToken(accessToken).getUser().getId());
        } catch (FlickrException e) {
            throw new ServiceException("mensagens.MSG-052", e);
        }
    }

    public void devinculaFlickr(String chaveEmpresa) {
        parametroService.set(chaveEmpresa, TipoParametro.FLICKR_ID, null);
    }
}
