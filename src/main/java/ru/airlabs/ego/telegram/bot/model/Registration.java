package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для регистрации пользователя
 *
 * @author Aleksey Gorbachev
 */
public class Registration {

    /**
     * ФИО пользователя
     */
    private String name;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Внешний идентификатор пользователя
     */
    private String socialUserId;

    /**
     * Пароль пользователя
     */
    private String password;

    /**
     * Повторение пароля пользователя
     */
    private String confirmPassword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSocialUserId() {
        return socialUserId;
    }

    public void setSocialUserId(String socialUserId) {
        this.socialUserId = socialUserId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
