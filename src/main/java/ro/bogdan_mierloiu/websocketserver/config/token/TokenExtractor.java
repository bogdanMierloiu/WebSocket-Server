package ro.bogdan_mierloiu.websocketserver.config.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import ro.bogdan_mierloiu.websocketserver.exception.BadCredentialException;
import ro.bogdan_mierloiu.websocketserver.exception.ParseFailedException;

import java.text.ParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenExtractor {

    private final TokenValidation tokenValidation;

    @Value("${auth.server.uri}")
    private String authServerIssuerUri;

    private Optional<String> extractBearerTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeaderValue = request.getHeader("Authorization");
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {
            String token = authorizationHeaderValue.substring(7);
            return Optional.of(token);
        }
        return Optional.empty();
    }

    public String getEmailFromToken(HttpServletRequest request) throws JsonProcessingException {
        final String token = extractBearerTokenFromRequest(request).orElseThrow(() -> new BadCredentialException("Invalid credentials"));
        final String issuerUri = getTokenIssuer(token);
        checkIssuers(issuerUri, token);
        Jwt jwt = decodeToken(token);
        return jwt.getClaimAsString("email");
    }

    public void checkIssuers(String issuerUri, String token) throws JsonProcessingException {
        if (issuerUri.equals(authServerIssuerUri)) {
            String serverResponse = tokenValidation.sendRequestToAuthServer(token, issuerUri);
            checkIfTokenIsActive(serverResponse);
        }
    }

    public void checkIfTokenIsActive(String serverResponse) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(serverResponse);
        if (!jsonNode.get("active").asBoolean()) {
            throw new BadCredentialException("Forbidden");
        }
    }

    public String getTokenIssuer(String token) {
        Jwt jwt = decodeToken(token);
        return jwt.getClaimAsString("iss");
    }

    public String getTokenAud(String token) {
        Jwt jwt = decodeToken(token);
        return jwt.getClaimAsString("aud");
    }

    public Jwt decodeToken(String token) {
        JwtDecoder jwtDecoder = new NimbusJwtDecoder(new ParseOnlyJWTProcessor());
        return jwtDecoder.decode(token);
    }

    private static class ParseOnlyJWTProcessor extends DefaultJWTProcessor<SecurityContext> {
        @Override
        public JWTClaimsSet process(SignedJWT jwt, SecurityContext context) {
            try {
                return jwt.getJWTClaimsSet();
            } catch (ParseException e) {
                throw new ParseFailedException("Failed to parse JWT");
            }
        }
    }
}
