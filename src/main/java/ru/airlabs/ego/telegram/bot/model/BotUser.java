package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для пользователя, использующего бот
 *
 * @author Aleksey Gorbachev
 */
public class BotUser {

    /**
     * Telegram ID
     */
    private long telegramId;

    /**
     * Пароль
     */
    private String password;

    /**
     * Текущее действие пользователя
     */
    private BotUserAction action;

    /**
     * Предыдущее действие пользователя
     */
    private BotUserAction prevAction;

    /**
     * Роль пользователя
     */
    private UserRole role;

    /**
     * Признак авторизации пользователя
     */
    private boolean isAuthorize = false;

    /**
     * Идентификатор вакансии, на которую будут отправляться приглашения
     */
    private Long vacancyId;

    /**
     * Данные по приглашению пользователя
     */
    private Invite invite;

    /**
     * Данные по регистрации нового пользователя
     */
    private Registration registration;

    /**
     * Настройки данных пользователя и компании
     */
    private AccountSettings settings;

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BotUserAction getAction() {
        return action;
    }

    public void setAction(BotUserAction action) {
        this.action = action;
    }

    public BotUserAction getPrevAction() {
        return prevAction;
    }

    public void setPrevAction(BotUserAction prevAction) {
        this.prevAction = prevAction;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isAuthorize() {
        return isAuthorize;
    }

    public void setAuthorize(boolean authorize) {
        isAuthorize = authorize;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
    }

    public Invite getInvite() {
        if (invite == null) {
            invite = new Invite();
        }
        return invite;
    }

    public void setInvite(Invite invite) {
        this.invite = invite;
    }

    public Registration getRegistration() {
        if (registration == null) {
            registration = new Registration();
        }
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    public AccountSettings getSettings() {
        return settings;
    }

    public void setSettings(AccountSettings settings) {
        this.settings = settings;
    }

    /**
     * Логаут пользователя в стартовое меню
     */
    public void logoutUser() {
        // отменяем авторизацию для пользователя
        setAuthorize(false);
        // очищаем данные пользователя
        setRole(null);
        setPassword(null);
        setVacancyId(null);
        setInvite(null);
    }
}
