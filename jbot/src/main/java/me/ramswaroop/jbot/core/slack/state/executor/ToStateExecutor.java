package me.ramswaroop.jbot.core.slack.state.executor;

import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.state.BotSession;
import me.ramswaroop.jbot.core.slack.state.Executor;

import java.util.regex.Matcher;

public class ToStateExecutor implements Executor {

    private String state;

    public ToStateExecutor(String state) {
        this.state = state;
    }

    @Override
    public void execute(BotSession botSession, Event event, Matcher matcher) {
        botSession.setState(state);
    }
}
