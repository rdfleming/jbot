package me.ramswaroop.jbot.core.slack.state;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Configuration
public class StateBotConfig {

    @Bean
    List<StateBotWebSocketHandler> handlers(ApplicationContext context) {
        Map<String, Object> bots = context.getBeansWithAnnotation(StateBot.class);
        final Random random= new Random(System.currentTimeMillis());
        return bots.values().stream().map(botObject -> {
            StateBotWebSocketHandler handler = new StateBotWebSocketHandler(botObject);
            context.getAutowireCapableBeanFactory().autowireBean(handler);
            context.getAutowireCapableBeanFactory().initializeBean(handler, "_webSocketHandler_"+System.currentTimeMillis()+random.nextLong());
            handler.init(context);
            return handler;
        }).collect(Collectors.toList());
    }
}
