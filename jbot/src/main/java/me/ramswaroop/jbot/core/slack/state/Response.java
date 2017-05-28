package me.ramswaroop.jbot.core.slack.state;

public class Response {

    private String state;
    private String reply;

    public Response() {
    }

    public Response(String state, String reply) {
        this.state = state;
        this.reply = reply;
    }

    public String getState() {
        return state;
    }

    public String getReply() {
        return reply;
    }
}
