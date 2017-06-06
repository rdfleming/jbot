package me.ramswaroop.jbot.core.slack.builder;

import me.ramswaroop.jbot.core.slack.state.CurrentState;
import me.ramswaroop.jbot.core.slack.state.Executor;
import me.ramswaroop.jbot.core.slack.state.StateBotWebSocketHandler;
import me.ramswaroop.jbot.core.slack.state.StateExecutorHandler;
import me.ramswaroop.jbot.core.slack.state.executor.ReplyExecutor;
import me.ramswaroop.jbot.core.slack.state.executor.StoreExecutor;
import me.ramswaroop.jbot.core.slack.state.executor.StoreGroupExecutor;

import java.util.ArrayList;
import java.util.List;

public class BotBuilder {

    private String slackToken;

    private String state;
    private int statusCount = 0;
    private List<StateExecutorHandler> stateExecutorList = new ArrayList<>();


    public BotBuilder(String slackToken) {
        this.slackToken = slackToken;
    }

    public PatternState fromStart() {
        state = CurrentState.START_STATE;
        return new PatternState(this);
    }

    public PatternState fromGlobal() {
        state = CurrentState.GLOBAL_STATE;
        return new PatternState(this);
    }

    public HelloState fromHello() {
        state = CurrentState.HELLO_STATE;
        return new HelloState(this);
    }

    public PatternState fromState(String state) {
        this.state = state;
        return new PatternState(this);
    }

    public StateBotWebSocketHandler build() {
        StateBotWebSocketHandler handler = new StateBotWebSocketHandler(slackToken);
        handler.addStateWrappers(stateExecutorList);
        return handler;
    }

    public class HelloState {

        private BotBuilder builder;

        public HelloState(BotBuilder builder) {
            this.builder = builder;
        }

        public BotBuilder reply(String value){
            stateExecutorList.add(
              new StateExecutorHandler(
                new ReplyExecutor(value, CurrentState.START_STATE),
                "(?s).*", state)
            );
            return builder;
        }
    }

    public class PatternState {

        private BotBuilder builder;

        PatternState(BotBuilder builder) {
            this.builder = builder;
        }

        public When when(String pattern) {
            return new When(builder, pattern);
        }
    }

    public class Store {

        private BotBuilder builder;
        private ReplyExecutor storeExecutor;
        private When when;

        Store(BotBuilder builder, When when, ReplyExecutor storeExecutor) {
            this.builder  = builder;
            this.when = when;
            this.storeExecutor = storeExecutor;
        }

        public Reply reply(String value){
            storeExecutor.setReply(value);
            return new Reply(builder, when, storeExecutor);
        }

    }

    public class Reply {

        private BotBuilder builder;
        private ReplyExecutor executor;
        private When when;

        Reply(BotBuilder builder, When when, ReplyExecutor executor) {
            this.builder = builder;
            this.when = when;
            this.executor = executor;
        }

        public PatternState andThen() {
            String newState = "STATE_"+statusCount++;
            executor.setState(newState);
            stateExecutorList.add(new StateExecutorHandler(executor, when.pattern, state));
            state = newState;
            return new PatternState(builder);
        }

        public When when(String pattern) {
            stateExecutorList.add(new StateExecutorHandler(executor, when.pattern, state));
            return new When(builder, pattern);
        }

        public BotBuilder toStartState(){
            executor.setState(CurrentState.START_STATE);
            stateExecutorList.add(new StateExecutorHandler(executor, when.pattern, state));
            return builder;
        }

        public BotBuilder toState(String state){
            executor.setState(state);
            stateExecutorList.add(new StateExecutorHandler(executor, when.pattern, state));
            return builder;
        }

    }

    public class When {

        private BotBuilder builder;
        private String pattern;

        When(BotBuilder builder, String pattern) {
            this.builder = builder;
            this.pattern = pattern;
        }

        public PatternState repeat(String value){
            stateExecutorList.add(new StateExecutorHandler(new ReplyExecutor(value, state), pattern, state));
            return new PatternState(builder);
        }

        public Store store(String key){
            return new Store(builder, this, new StoreExecutor(key));
        }

        public Store store(int group, String key){
            return new Store(builder, this, new StoreGroupExecutor(key, group));
        }

        public Reply reply(String value){
            return new Reply(builder, this, new ReplyExecutor(value));
        }

        public BotBuilder handle(Executor executor){
            stateExecutorList.add(new StateExecutorHandler(executor, pattern, state));
            return builder;
        }

    }


}
