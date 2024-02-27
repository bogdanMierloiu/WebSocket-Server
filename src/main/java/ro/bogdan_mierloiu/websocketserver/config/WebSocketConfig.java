package ro.bogdan_mierloiu.websocketserver.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import ro.bogdan_mierloiu.websocketserver.config.token.TokenExtractor;
import ro.bogdan_mierloiu.websocketserver.config.token.TokenHolder;
import ro.bogdan_mierloiu.websocketserver.dto.User;
import ro.bogdan_mierloiu.websocketserver.exception.BadCredentialException;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenExtractor tokenExtractor;
    private static final String FORBIDDEN = "Forbidden";

    /**
     * Registers the STOMP endpoint, enabling SockJS fallback options with allowed origins.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers the STOMP endpoint, enabling SockJS fallback options with allowed origins.
     */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-app").setAllowedOriginPatterns("*");
    }

    /**
     * Adds a custom interceptor to the channel to handle the CONNECT and SUBSCRIBE commands.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        TokenHolder tokenHolder = new TokenHolder();
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message,
                                      @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    handleConnectCommand(accessor, tokenHolder);
                }
                if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    handleSubscribeCommand(message, tokenHolder);
                }

                return message;
            }
        });
    }

    /**
     * Handles the CONNECT command, extracts and validates the token with authentication server, and sets the user principal.
     */
    private void handleConnectCommand(StompHeaderAccessor accessor, TokenHolder tokenHolder) {
        String token = extractTokenFromHeader(accessor);
        tokenHolder.setToken(token);

        String issuerUri = tokenExtractor.getTokenIssuer(token);
        try {
            tokenExtractor.checkIssuers(issuerUri, token);
        } catch (Exception e) {
            throw new BadCredentialException(FORBIDDEN);
        }

        Jwt jwt = tokenExtractor.decodeToken(token);
        accessor.setUser(buildUserFromToken(jwt));
    }

    /**
     * Handles the SUBSCRIBE command, validates the subscription request, and checks if the topic
     * requested for subscription matches the 'aud' claim in the token used for connection.
     */
    private void handleSubscribeCommand(Message<?> message, TokenHolder tokenHolder) {
        String token = tokenHolder.getToken();
        String destination = (String) message.getHeaders().get(SimpMessageHeaderAccessor.DESTINATION_HEADER);

        if (destination == null) {
            throw new BadCredentialException(FORBIDDEN);
        }

        String clientFromDestination = extractClientFromDestination(destination);
        String tokenAud = extractTokenAudience(token);

        if (!clientFromDestination.equals(tokenAud)) {
            throw new BadCredentialException(FORBIDDEN);
        }
    }

    /**
     * Builds a user object from the token's claims.
     */

    private User buildUserFromToken(Jwt jwt) {
        return User.builder()
                .email(jwt.getClaimAsString("email"))
                .name(jwt.getClaimAsString("name"))
                .surname(jwt.getClaimAsString("surname"))
                .build();
    }

    /**
     * Extracts specific field from user's request header.
     */
    private String extractTokenFromHeader(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("token");
        if (token == null || token.isEmpty()) {
            throw new BadCredentialException(FORBIDDEN);
        }
        return token;
    }

    /**
     * Extracts the client from the destination.
     */
    private String extractClientFromDestination(String destination) {
        return destination.substring(7, destination.indexOf('/', 7));
    }

    /**
     * Extracts the 'aud' claim from the token.
     */
    private String extractTokenAudience(String token) {
        return tokenExtractor.getTokenAud(token).replace("[", "").replace("]", "");
    }

}