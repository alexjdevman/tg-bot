package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для передачи данных по приглашению пользователя
 *
 * @author Aleksey Gorbachev
 */
public class Invite {

    /**
     * ФИО пользователя
     */
    private String name;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Телефон пользователя
     */
    private String phone;

    /**
     * Признак Email-приглашения
     */
    private boolean emailInvite = false;

    /**
     * Признак аудио-приглашения
     */
    private boolean audioInvite = false;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEmailInvite() {
        return emailInvite;
    }

    public void setEmailInvite(boolean emailInvite) {
        this.emailInvite = emailInvite;
    }

    public boolean isAudioInvite() {
        return audioInvite;
    }

    public void setAudioInvite(boolean audioInvite) {
        this.audioInvite = audioInvite;
    }
}
