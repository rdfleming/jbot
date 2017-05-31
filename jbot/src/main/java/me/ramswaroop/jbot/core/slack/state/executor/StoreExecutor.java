package me.ramswaroop.jbot.core.slack.state.executor;

import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.state.BotSession;

import java.util.regex.Matcher;

public class StoreExecutor extends ReplyExecutor {

    private String storeKey;

    public StoreExecutor(String storeKey) {
        this.storeKey = storeKey;
    }

    @Override
    public void execute(BotSession botSession, Event event, Matcher matcher) {
        botSession.getCurrentState().getConversationMap().put(storeKey, event.getText().trim());
        super.execute(botSession, event, matcher);
    }

}
