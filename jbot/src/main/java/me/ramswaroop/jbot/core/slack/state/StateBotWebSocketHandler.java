package me.ramswaroop.jbot.core.slack.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateBotWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(StateBotWebSocketHandler.class);

    @Autowired
    protected SlackService slackService;

    private ObjectMapper mapper = new ObjectMapper();
    private Object stateBot;

    private Map<String, List<StateExecutor>> stateMap = new HashMap<>();
    private Map<String, CurrentState> currentStateMap = new ConcurrentHashMap<>();

    public StateBotWebSocketHandler(Object stateBot) {
        this.stateBot = stateBot;


        Method[] methods = stateBot.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(StateController.class)) {
                StateController state = method.getAnnotation(StateController.class);
                addStateWrapper(new StateExecutor(method, state));
            }
        }

    }

    public void init(ApplicationContext applicationContext) {
        if (stateBot.getClass().isAnnotationPresent(StateBot.class)) {
            StateBot sBot = stateBot.getClass().getAnnotation(StateBot.class);
            String token = applicationContext.getEnvironment().resolvePlaceholders(sBot.slackToken());
            slackService.startRTM(token);
        }
        if (slackService.getWebSocketUrl() != null) {
            WebSocketConnectionManager manager = new WebSocketConnectionManager(client(), this, slackService.getWebSocketUrl());
            manager.start();
        } else {
            logger.error("No websocket url returned by Slack.");
        }
    }

    private void addStateWrapper(StateExecutor stateExecutor) {
        List<StateExecutor> stateList = stateMap
          .computeIfAbsent(stateExecutor.getState(),
            k -> new ArrayList<>());
        stateList.add(stateExecutor);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Event event = mapper.readValue(textMessage.getPayload(), Event.class);
        slackService.enrichEvent(event);
        if (event.isType(EventType.DIRECT_MESSAGE)) {
            CurrentState currentState = currentStateMap
              .computeIfAbsent(event.getChannelId(),
                k -> new CurrentState(CurrentState.HELLO_STATE));

            execute(currentState, session, event);
        } else {
            logger.debug("Received type {}", event.getType());
        }
    }

    private void execute(CurrentState currentState , WebSocketSession session, Event event) {
        // Handle GLOBAL state first
        List<StateExecutor> executorList = stateMap.get(CurrentState.GLOBAL_STATE);
        if (executorList != null) {
            for (StateExecutor stateExecutor : executorList) {
                if (stateExecutor.handle(stateBot, session, event, currentState)) {
                    logger.debug("Executed global state handler");
                    return;
                }
            }
        }

        executorList = stateMap.get(currentState.getState());
        if (executorList == null) {
            logger.error("Cannot find state {}", currentState.getState());
            currentState.setState(CurrentState.START_STATE);
            return;
        }

        for (StateExecutor stateExecutor : executorList) {
            if (stateExecutor.handle(stateBot, session, event, currentState)) {
                logger.debug("Executed state handler for state: {}", stateExecutor.getState());
                return;
            }
        }
        logger.warn("Cannot find a wrapper that matches {}", currentState.getState());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.error("Binary messages are not supported in Slack RTM API");
    }

    public void afterConnectionEstablished(WebSocketSession session) {
        logger.debug("WebSocket connected: {}", session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.debug("WebSocket closed: {}, Close Status: {}", session, status.toString());
    }

    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("Transport Error: {}", exception);
    }

    private StandardWebSocketClient client() {
        return new StandardWebSocketClient();
    }

}
