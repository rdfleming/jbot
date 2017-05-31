package me.ramswaroop.jbot.core.slack.state.executor;

import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.state.BotSession;
import me.ramswaroop.jbot.core.slack.state.Executor;

import java.util.regex.Matcher;

public class ReplyExecutor implements Executor {

    private String reply;
    private String state;

    public ReplyExecutor() {
    }

    public ReplyExecutor(String reply) {
        this.reply = reply;
    }

    public ReplyExecutor(String reply, String state) {
        this.reply = reply;
        this.state = state;
    }

    @Override
    public void execute(BotSession botSession, Event event, Matcher matcher) {
        botSession.reply(event, reply);
        botSession.setState(state);
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public void setState(String state) {
        this.state = state;
    }
}
