package me.ramswaroop.jbot.core.slack.state;

import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class BotSession {

    private static final Logger logger = LoggerFactory.getLogger(BotSession.class);

    private WebSocketSession socketSession;
    private CurrentState currentState;

    public BotSession(WebSocketSession socketSession, CurrentState currentState) {
        this.socketSession = socketSession;
        this.currentState = currentState;
    }

    public void reply(Event event, String text) {
        try {
            Message reply = new Message(text);
            reply.setType(EventType.MESSAGE.name().toLowerCase());
            reply.setText(encode(reply.getText()));
            if (reply.getChannel() == null && event.getChannelId() != null) {
                reply.setChannel(event.getChannelId());
            }
            socketSession.sendMessage(new TextMessage(reply.toJSONString()));
            if (logger.isDebugEnabled()) {  // For debugging purpose only
                logger.debug("Reply (Message): {}", reply.toJSONString());
            }
        } catch (IOException e) {
            logger.error("Error sending event: {}. Exception: {}", event.getText(), e.getMessage());
        }
    }

    public void setState(String state) {
        currentState.setState(state);
    }

    private String encode(String message) {
        return message == null ? null : message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public WebSocketSession getSocketSession() {
        return socketSession;
    }

    public CurrentState getCurrentState() {
        return currentState;
    }
}
