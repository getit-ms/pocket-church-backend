package br.gafs.calvinista.servidor.mensagem;


import br.gafs.calvinista.entity.Igreja;
import br.gafs.calvinista.entity.TokenFirebase;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.dao.DAOService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseTokenProviderTest {

    public static final String TOKEN_PADRAO = "TOKEN_PADRAO";
    @Mock
    private DAOService daoService;

    @Mock
    private ParametroService parametroService;

    @InjectMocks
    private FirebaseTokenProvider firebaseTokenProvider;

    private TokenFirebase TOKEN1 = new TokenFirebase(
            1L,
            "7.1.8",
            "TOKEN1",
            new Igreja("tst")
    );

    private TokenFirebase TOKEN2 = new TokenFirebase(
            2L,
            "6.0.8",
            "TOKEN2",
            new Igreja("tst")
    );

    private TokenFirebase TOKEN3 = new TokenFirebase(
            3L,
            "7.1.10",
            "TOKEN3",
            new Igreja("tst")
    );

    @Before
    public void prepara() {
        Mockito.when(daoService.findAll(TokenFirebase.class))
                .thenReturn(
                        Arrays.asList(
                                TOKEN1, TOKEN2, TOKEN3
                        )
                );

        Mockito.when(parametroService.get(Mockito.any(String.class), Mockito.any(TipoParametro.class)))
                .thenReturn(TOKEN_PADRAO);

        firebaseTokenProvider.cacheTokens();
    }

    @Test
    public void testGetToken() {

        Assert.assertEquals(TOKEN_PADRAO, firebaseTokenProvider.getToken("tst2", "7.1.10"));
        Assert.assertEquals(TOKEN_PADRAO, firebaseTokenProvider.getToken("tst", "1.0.0"));
        Assert.assertEquals(TOKEN_PADRAO, firebaseTokenProvider.getToken("tst", "6.0.7"));

        Assert.assertEquals(TOKEN1.getToken(), firebaseTokenProvider.getToken("tst", "7.1.8"));
        Assert.assertEquals(TOKEN1.getToken(), firebaseTokenProvider.getToken("tst", "7.1.9"));

        Assert.assertEquals(TOKEN2.getToken(), firebaseTokenProvider.getToken("tst", "6.0.8"));
        Assert.assertEquals(TOKEN2.getToken(), firebaseTokenProvider.getToken("tst", "7.1.7"));

        Assert.assertEquals(TOKEN3.getToken(), firebaseTokenProvider.getToken("tst", "7.1.10"));
        Assert.assertEquals(TOKEN3.getToken(), firebaseTokenProvider.getToken("tst", "8.0.0"));

    }

}
