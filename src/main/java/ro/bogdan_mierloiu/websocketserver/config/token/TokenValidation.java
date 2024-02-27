package ro.bogdan_mierloiu.websocketserver.config.token;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component

public class TokenValidation {

    @Value("${client-id}")
    private String clientId;

    @Value("${client-secret}")
    private String clientSecret;

    /**
     * Sends a request to the authentication server to check if the token is valid.
     *
     * @param token   the token to be checked for validity
     * @param authUri the authentication server uri
     * @return the response from the authentication server
     */
    public String sendRequestToAuthServer(String token, String authUri) {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + CredentialEncoder.encodeCredentials(clientId, clientSecret));
        MultiValueMap<String, String> parametersMap = new LinkedMultiValueMap<>();
        parametersMap.add("token", token);
        HttpEntity<MultiValueMap<String, String>> postRequest = new HttpEntity<>(parametersMap, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(authUri + "/oauth2/introspect",
                postRequest, String.class);
        return response.getBody();
    }
}
