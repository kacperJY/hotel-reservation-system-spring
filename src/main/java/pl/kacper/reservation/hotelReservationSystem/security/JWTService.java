package pl.kacper.reservation.hotelReservationSystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTService {

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(),userDetails.getUsername());
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        String username = userDetails.getUsername();
        String tokenPayloadUsername = extractUsername(token);
        Date expiration = extractExpiration(token);

        return (username.equals(tokenPayloadUsername) && new Date().before(expiration));
    }

    private String generateToken(Map<String, Object> claims, String username){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private Date extractExpiration(String token){
        return extractSingleClaim(token, claims -> claims.<Date>getExpiration());
    }

    public String extractUsername(String token){
        return extractSingleClaim(token, claims -> claims.<String>getSubject());
    }

    private  <T> T extractSingleClaim(String token, Function<Claims, T> function){
        Claims allClaims = getAllClaims(token);
        return function.apply(allClaims);
    }

    private Claims getAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
