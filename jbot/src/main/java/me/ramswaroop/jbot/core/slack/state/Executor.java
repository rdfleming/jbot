package me.ramswaroop.jbot.core.slack.state;

import me.ramswaroop.jbot.core.slack.models.Event;

import java.util.regex.Matcher;

public interface Executor {

    void execute(BotSession botSession, Event event, Matcher matcher);
}
