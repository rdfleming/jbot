package me.ramswaroop.jbot.core.slack.state;

import me.ramswaroop.jbot.core.slack.models.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StateExecutor {

    private static final Logger logger = LoggerFactory.getLogger(StateExecutor.class);

    private String state;
    private Pattern pattern;
    private Executor executor;

    public StateExecutor( Executor executor, String patternString, String state) {
        this.state = state;
        this.executor = executor;

        if (StringUtils.isEmpty(patternString)) patternString=".*"; // default pattern
        pattern = Pattern.compile(patternString);
    }

    public boolean handle(WebSocketSession session, Event event, CurrentState currentState) {
        Matcher matcher = pattern.matcher(event.getText());
        if (matcher.matches()) {
            BotSession botSession = new BotSession(session,currentState);
            try {
                executor.execute(botSession, event, matcher);
            } catch (Exception e) {
                logger.error("Error running Executor: {}->{}", currentState.getState(), pattern.pattern(), e);
            }

            return true;
        }
        return false;
    }
    
    public String getState() {
        return state;
    }
}
