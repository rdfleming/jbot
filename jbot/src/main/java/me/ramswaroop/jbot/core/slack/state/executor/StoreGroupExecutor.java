package me.ramswaroop.jbot.core.slack.state.executor;

import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.state.BotSession;

import java.util.regex.Matcher;

public class StoreGroupExecutor extends ReplyExecutor {

    private String storeKey;
    private int group;

    public StoreGroupExecutor(String storeKey, int group) {
        this.storeKey = storeKey;
        this.group = group;
    }

    @Override
    public void execute(BotSession botSession, Event event, Matcher matcher) {
        botSession.getCurrentState().getConversationMap().put(storeKey, matcher.group(group));
        super.execute(botSession, event, matcher);

    }

}
