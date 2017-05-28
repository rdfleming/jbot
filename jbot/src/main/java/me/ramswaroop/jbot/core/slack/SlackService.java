package me.ramswaroop.jbot.core.slack;

import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.RTM;
import me.ramswaroop.jbot.core.slack.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ramswaroop
 * @version 14/08/2016
 */
@Service
@Scope("prototype")
public class SlackService {

    @Autowired
    private SlackDao slackDao;
    private User currentUser;
    private List<String> dmChannels;
    private String webSocketUrl;

    /**
     * Start a RTM connection. Fetch the web socket url to connect to, current user details
     * and list of channel ids where the current user has had conversation.
     *
     * @param slackToken
     */
    public void startRTM(String slackToken) {
        RTM rtm = slackDao.startRTM(slackToken);
        currentUser = rtm.getUser();
        dmChannels = rtm.getDmChannels();
        webSocketUrl = rtm.getWebSocketUrl();
    }

    /**
     * @return user representing the bot.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * @return list of channel ids where the current user has had conversation.
     */
    public List<String> getDmChannels() {
        return dmChannels;
    }

    public void setDmChannels(List<String> dmChannels) {
        this.dmChannels = dmChannels;
    }

    public boolean addDmChannel(String channelId) {
        if (dmChannels == null) dmChannels = new ArrayList<>();
        return dmChannels.add(channelId);
    }

    /**
     * @return web socket url to connect to.
     */
    public String getWebSocketUrl() {
        return webSocketUrl;
    }

    public void setWebSocketUrl(String webSocketUrl) {
        this.webSocketUrl = webSocketUrl;
    }

    public void enrichEvent(Event event) {
        if (event.getType() != null) {
            if (event.getType().equalsIgnoreCase(EventType.IM_OPEN.name())) {
                addDmChannel(event.getChannelId());
            } else
            if (event.getType().equalsIgnoreCase(EventType.MESSAGE.name())) {
                if (event.getText() != null && event.getText().contains(getCurrentUser().getId())) { // direct mention
                    event.setType(EventType.DIRECT_MENTION.name());
                } else if (getDmChannels().contains(event.getChannelId())) { // direct message
                    event.setType(EventType.DIRECT_MESSAGE.name());
                }
            }
        } else { // slack does not send any TYPE for acknowledgement messages
            event.setType(EventType.ACK.name());
        }

    }
}
