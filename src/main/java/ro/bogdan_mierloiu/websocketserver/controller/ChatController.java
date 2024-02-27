package ro.bogdan_mierloiu.websocketserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;
import ro.bogdan_mierloiu.websocketserver.dto.Message;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {


    @MessageMapping("/chat")
    @SendTo("/topic/notification-client/messages")
    public Message receiveSendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        assert principal != null;
        return Message.builder()
                .messageContent(principal.getName() + ": " + HtmlUtils.htmlEscape(message.messageContent()))
                .build();
    }

}