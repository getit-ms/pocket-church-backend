/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Base64;
import java.util.ResourceBundle;
import javax.crypto.spec.SecretKeySpec;


/**
 *
 * @author Gabriel
 */
public final class JWTManager {
    private static final Key key;
    private static final SignatureAlgorithm algorithm;

    static {
        ResourceBundle secret = ResourceBundle.getBundle("jwt-secret");

        algorithm = getSignatureAlgorithm(secret.getString("key.algorithm"));
        key = new SecretKeySpec(Base64.getDecoder().decode(secret.getString("key.base64")), algorithm.getValue());
    }
	
    private static SignatureAlgorithm getSignatureAlgorithm(String name) {
        for (SignatureAlgorithm sa : SignatureAlgorithm.values()){
            if (name.equals(sa.getJcaName())){
                    return sa;
            }
        }
        return SignatureAlgorithm.HS512;
    }
    
    public static JWTWriter writer(){
        return new JWTWriter();
    }
    
    public static JWTReader reader(String jwt){
        return new JWTReader(jwt);
    }

    private JWTManager(){}

    public static class JWTReader { 
        private Jws<Claims> claims;
        
        private JWTReader(String jwt){
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
        }
        
        public Object get(String key){
            return claims.getBody().get(key);
        }
    }

    public static class JWTWriter { 
        private JwtBuilder builder = Jwts.builder();
        
        private JWTWriter(){}
        
        public JWTWriter map(String key, Object val){
            builder.claim(key, val);
            return this;
        }

        public String build(){
            return builder.signWith(SignatureAlgorithm.HS512, key).compact();
        }
    }
    
}
