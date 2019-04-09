package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для управления пользователями
 *
 * @author Aleksey Gorbachev
 */
public class User {

    /**
     * Идентификатор Telegram
     */
    private String socialUserId;

    /**
     * Идентификатор владельца (Owner)
     */
    private Long ownerId;

    /**
     * ФИО пользователя
     */
    private String name;

    /**
     * Пароль
     */
    private String password;

    public String getSocialUserId() {
        return socialUserId;
    }

    public void setSocialUserId(String socialUserId) {
        this.socialUserId = socialUserId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return name + ", ID: " + socialUserId + ", пароль: " + password;
    }
}
