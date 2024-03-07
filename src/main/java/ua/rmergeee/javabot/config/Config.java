package ua.rmergeee.javabot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Value("${bot.username}")
    private String username;
    @Value("${bot.token}")
    private String token;

    public String getUsername() {
        return username;
    }
    public String getToken() {
        return token;
    }
}
