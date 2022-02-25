/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.util;

import br.gafs.calvinista.entity.Parametro;
import br.gafs.calvinista.entity.domain.TipoParametro;
import br.gafs.calvinista.service.ParametroService;
import io.jsonwebtoken.*;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.security.Key;
import java.util.Base64;


/**
 * @author Gabriel
 */
@Singleton
public class JWTManager {

    @EJB
    private ParametroService parametroService;

    private Key key;
    private SignatureAlgorithm algorithm;

    @PostConstruct
    @Schedule(hour = "*", persistent = false)
    public void prepara() {
        algorithm = getSignatureAlgorithm((String) parametroService.get(Parametro.GLOBAL, TipoParametro.JWT_KEY_ALGORITHM));
        key = new SecretKeySpec(Base64.getDecoder().decode((String) parametroService.get(Parametro.GLOBAL, TipoParametro.JWT_KEY)), algorithm.getValue());
    }

    private SignatureAlgorithm getSignatureAlgorithm(String name) {
        for (SignatureAlgorithm sa : SignatureAlgorithm.values()) {
            if (name.equals(sa.getJcaName())) {
                return sa;
            }
        }
        return SignatureAlgorithm.HS512;
    }

    public JWTWriter writer() {
        return new JWTWriter();
    }

    public JWTReader reader(String jwt) {
        return new JWTReader(jwt);
    }

    public class JWTReader {
        private Jws<Claims> claims;

        private JWTReader(String jwt) {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
        }

        public Object get(String key) {
            return claims.getBody().get(key);
        }
    }

    public class JWTWriter {
        private JwtBuilder builder = Jwts.builder();

        private JWTWriter() {
        }

        public JWTWriter map(String key, Object val) {
            builder.claim(key, val);
            return this;
        }

        public String build() {
            return builder.signWith(SignatureAlgorithm.HS512, key).compact();
        }
    }

}
