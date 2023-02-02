package br.gafs.calvinista.servidor.mensagem;

import br.gafs.calvinista.dao.CustomDAOService;
import br.gafs.calvinista.entity.TokenFirebase;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import br.gafs.util.string.StringUtil;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.util.*;

@Singleton
public class FirebaseTokenProvider {

    @EJB
    private CustomDAOService daoService;

    @EJB
    private ParametroService parametroService;

    private TokensDTO tokens;

    @PostConstruct
    @Schedule(hour = "*", persistent = false)
    public void cacheTokens() {
        this.tokens = new TokensDTO(daoService.findAll(TokenFirebase.class));
    }

    public String getToken(String chaveIgreja, String versao) {
        VersoesDTO igreja = tokens.getIgreja(chaveIgreja);

        if (igreja != null) {
            String token = igreja.getVersao(versao);

            if (!StringUtil.isEmpty(token)) {
                return token;
            }
        }

        return parametroService.get(chaveIgreja, TipoParametro.PUSH_ANDROID_KEY);
    }

    class TokensDTO {
        private Map<String, VersoesDTO> versoes = new HashMap<>();

        TokensDTO(List<TokenFirebase> tokens) {
            for (TokenFirebase token : tokens) {
                String chaveIgreja = token.getIgreja() != null ? token.getIgreja().getChave() : null;

                if (!versoes.containsKey(chaveIgreja)) {
                    versoes.put(chaveIgreja, new VersoesDTO());
                }

                versoes.get(chaveIgreja).add(token);
            }
        }

        public VersoesDTO getIgreja(String chaveIgreja) {
            if (!versoes.containsKey(chaveIgreja)) {
                return versoes.get(null);
            }

            return versoes.get(chaveIgreja);
        }
    }

    class VersoesDTO {
        private List<TokenVersaoDTO> tokens = new ArrayList<>();

        public String getVersao(String versao) {
            TokenVersaoDTO other = new TokenVersaoDTO(versao);

            for (TokenVersaoDTO token : tokens) {
                if (token.matches(other)) {
                    return token.getToken();
                }
            }

            return null;
        }

        public void add(TokenFirebase token) {
            tokens.add(new TokenVersaoDTO(token.getVersao(), token.getToken()));

            Collections.sort(tokens);
        }
    }

    class TokenVersaoDTO implements Comparable<TokenVersaoDTO> {
        @Getter
        private String token;

        private int major;
        private int minor;
        private int bugfix;

        TokenVersaoDTO(String versao) {
            versao = StringUtil.isEmpty(versao) || !versao.matches("\\d+\\.\\d+\\.\\d+") ? "0.0.0" : versao;

            String[] parts = versao.split("\\.");

            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.bugfix = Integer.parseInt(parts[2]);
        }

        TokenVersaoDTO(String versao, String token) {
            this(versao);
            this.token = token;
        }

        public boolean matches(TokenVersaoDTO versao) {
            // atual deve ser menor ou igual a versao

            return major < versao.major ||
                    (major == versao.major && minor < versao.minor) ||
                    (major == versao.major && minor == versao.minor && bugfix <= versao.bugfix);
        }

        @Override
        public int compareTo(TokenVersaoDTO o) {
            int comp = o.major - major;

            if (comp == 0) {
                comp = o.minor - minor;

                if (comp == 0) {
                    comp = o.bugfix - bugfix;
                }
            }

            return comp;
        }
    }
}
