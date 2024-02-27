package ro.bogdan_mierloiu.websocketserver.config.token;

import java.util.Base64;

public class CredentialEncoder {

    private CredentialEncoder() {
    }

    public static String encodeCredentials(String username, String password) {
        String notEncodedCredentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(notEncodedCredentials.getBytes());
    }
}
