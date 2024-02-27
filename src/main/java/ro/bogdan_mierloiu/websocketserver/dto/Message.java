package ro.bogdan_mierloiu.websocketserver.dto;

import lombok.Builder;

@Builder
public record Message(
        String messageContent
) {
}