package me.ramswaroop.jbot.core.slack.state;

import me.ramswaroop.jbot.core.slack.BotException;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StateExecutor {

    private static final Logger logger = LoggerFactory.getLogger(StateExecutor.class);

    private String state;
    private Pattern pattern;
    private Method method;
    private boolean containsMatcher = false;

    public StateExecutor(Method method, StateController state) {
        this.method = method;
        this.state = state.state();

        for (Class paramClass : method.getParameterTypes()) {
            if (paramClass.equals(Matcher.class)) {
                containsMatcher = true;
                break;
            }
        }

        String patternString = state.pattern();
        if (StringUtils.isEmpty(patternString)) patternString=".*"; // default pattern
        pattern = Pattern.compile(patternString);
    }

    public boolean handle(Object bot, WebSocketSession session, Event event, CurrentState currentState) {
        Matcher matcher = pattern.matcher(event.getText());
        if (matcher.matches()) {
            try {
                Object response;
                if (containsMatcher) {
                    response = method.invoke(bot, session, event, currentState, matcher);
                } else {
                    response = method.invoke(bot, session, event, currentState);
                }
                if (response != null && !(response instanceof Void)) {
                    if (response instanceof String) { // State stays the same
                        if (currentState.getState().equals(CurrentState.HELLO_STATE)) {
                            currentState.setState(CurrentState.START_STATE);
                        }
                        reply(session, event, (String)response);
                    } else
                    if (response instanceof Response) {
                        Response stateResponse = (Response)response;
                        if (stateResponse.getState() != null) currentState.setState(stateResponse.getState());

                        reply(session, event, stateResponse.getReply());
                    } else {
                        logger.warn("Unknown response from bot method {} :{}", method.getName(), response.getClass());
                    }
                }
                return true;
            } catch (Exception e) {
                throw new BotException("Method invocation error: " + method.getName(), e);
            }
        }
        return false;
    }

    private void reply(WebSocketSession session, Event event, String text) {
        try {
            Message reply = new Message(text);
            reply.setType(EventType.MESSAGE.name().toLowerCase());
            reply.setText(encode(reply.getText()));
            if (reply.getChannel() == null && event.getChannelId() != null) {
                reply.setChannel(event.getChannelId());
            }
            session.sendMessage(new TextMessage(reply.toJSONString()));
            if (logger.isDebugEnabled()) {  // For debugging purpose only
                logger.debug("Reply (Message): {}", reply.toJSONString());
            }
        } catch (IOException e) {
            logger.error("Error sending event: {}. Exception: {}", event.getText(), e.getMessage());
        }
    }

    private String encode(String message) {
        return message == null ? null : message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }


    public Matcher matches(String text) {
        return pattern.matcher(text);
    }

    public String getState() {
        return state;
    }
}
