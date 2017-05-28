package me.ramswaroop.jbot.core.slack.state;

import java.util.HashMap;
import java.util.Map;

public class CurrentState {

    public static final String GLOBAL_STATE = "GLOBAL";
    public static final String START_STATE = "START";
    public static final String HELLO_STATE = "HELLO";

    private String state;
    private Map<String, Object> stateMap = new HashMap<>();

    public CurrentState() {
    }

    public CurrentState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getConversationMap() {
        return stateMap;
    }
}
