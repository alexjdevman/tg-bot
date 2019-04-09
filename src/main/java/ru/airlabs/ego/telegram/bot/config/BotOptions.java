package ru.airlabs.ego.telegram.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

/**
 * Дополнительные настройки для Telegram-бота
 *
 * @author Aleksey Gorbachev
 */
@Component
public class BotOptions extends DefaultBotOptions {

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private int proxyPort;

    @Value("${proxy.type}")
    private ProxyType proxyType;

    @Override
    public String getProxyHost() {
        return proxyHost;
    }

    @Override
    public int getProxyPort() {
        return proxyPort;
    }

    @Override
    public ProxyType getProxyType() {
        return proxyType;
    }
}
