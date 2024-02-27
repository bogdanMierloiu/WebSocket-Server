package ro.bogdan_mierloiu.websocketserver.config.token;

/**
 * A simple holder for the token, allowing access to the token across method calls in the interceptor.
 */
public class TokenHolder {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
