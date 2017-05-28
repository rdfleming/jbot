package me.ramswaroop.jbot.core.slack.state;

public class State {

    // Default
    public static final State START_STATE = new State("START");

    private String name;

    public State(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof State)) {
            return false;
        }

        State state = (State) o;

        return name.equals(state.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
}
