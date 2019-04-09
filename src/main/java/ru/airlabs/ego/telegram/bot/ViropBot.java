package ru.airlabs.ego.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.airlabs.ego.telegram.bot.model.*;
import ru.airlabs.ego.telegram.bot.service.BotHttpRequestService;
import ru.airlabs.ego.telegram.bot.service.BotMessageService;
import ru.airlabs.ego.telegram.bot.util.EmailUtils;
import ru.airlabs.ego.telegram.bot.util.PhoneUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Реализация Telegram-бота
 *
 * @author Aleksey Gorbachev
 */
@Component
public class ViropBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViropBot.class);

    /**
     * Карта для хранения действий пользователей (ключ - идентификатор чата пользователя)
     */
    public static final Map<Long, BotUser> botActionMap = new ConcurrentHashMap<>();

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Autowired
    private BotHttpRequestService httpRequestService;

    @Autowired
    public ViropBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            final long chatId = update.getMessage().getChatId();

            switch (messageText) {  // обработка команд и сообщений
                case "/start":
                    sendMessage(BotMessageService.getStartMenuMessage(chatId));
                    setStartAction(chatId);
                    break;
                default:
                    processNonCommandMessage(update.getMessage());
                    break;
            }
        } else if (update.hasCallbackQuery()) { // обработка callback-ов
            processCallbackQuery(update.getCallbackQuery());
        }
    }

    /**
     * Регистрация и запуск бота
     */
    @PostConstruct
    public void registerBot() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Обработка сообщения не начинающегося с '/'
     *
     * @param message сообщение
     */
    public void processNonCommandMessage(Message message) {
        String messageText = message.getText();
        final long chatId = message.getChatId();
        BotUser user = botActionMap.get(chatId);
        if (user != null) { // получаем текущего пользователя
            BotUserAction action = user.getAction();
            switch (action) {
                case LOGIN: // обработка авторизации пользователя
                    loginUser(user, chatId, messageText);
                    break;
                case INVITATION_VACANCY: // обработка ввода данных пользователя (имя) для приглашения
                    processInvitationUserNameInput(chatId, user, messageText);
                    break;
                case INVITATION_USER_NAME: // обработка ввода данных пользователя (телефон) для приглашения
                    processInvitationUserPhoneInput(chatId, user, messageText);
                    break;
                case INVITATION_USER_PHONE: // обработка ввода данных пользователя (Email) для приглашения
                    processInvitationUserEmailInput(chatId, user, messageText);
                    break;
                case REGISTRATION:  // обработка ввода имени пользователя при регистрации
                    processRegistrationUserNameInput(chatId, user, messageText);
                    break;
                case REGISTRATION_USER_NAME:  // обработка ввода Email пользователя при регистрации
                    processRegistrationUserEmailInput(chatId, user, messageText);
                    break;
                case REGISTRATION_USER_EMAIL:  // обработка ввода пароля пользователя при регистрации
                    processRegistrationUserPasswordInput(chatId, user, messageText);
                    break;
                case SETTINGS_USERS_ADD:    // обработка ввода ФИО пользователя при добавлении нового пользователя
                    processAddNewUser(chatId, user, messageText);
                    break;
                case SETTINGS_USERS_DELETE:    // обработка ввода ID пользователя при удалении пользователя
                    processUserIdInput(chatId, user, messageText);
                    break;
                case SETTINGS_COMPANY_FIO:    // обработка ввода ФИО пользователя для данных компании
                    processSettingsCompanyFioInput(chatId, user, messageText);
                    break;
                case SETTINGS_COMPANY_PHONE:    // обработка ввода телефона для данных компании
                    processSettingsCompanyPhoneInput(chatId, user, messageText);
                    break;
                case SETTINGS_COMPANY_SITE:    // обработка ввода сайта для данных компании
                    processSettingsCompanySiteInput(chatId, user, messageText);
                    break;
                case SETTINGS_COMPANY_ADDRESS: // обработка ввода адреса для данных компании
                    processSettingsCompanyAddressInput(chatId, user, messageText);
                    break;
                case SETTINGS_COMPANY_NAME:    // обработка ввода названия компании для данных компании
                    processSettingsCompanyNameInput(chatId, user, messageText);
                    break;
                case SETTINGS_COMPANY_DESCRIPTION:    // обработка ввода описания компании для данных компании
                    processSettingsCompanyDescriptionInput(chatId, user, messageText);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Обработка callback-ов (нажатия на кнопки)
     *
     * @param query callback
     */
    public void processCallbackQuery(CallbackQuery query) {
        String callback = query.getData();
        final long chatId = query.getMessage().getChatId();
        BotUser user = botActionMap.get(chatId);

        switch (callback) {
            case "/login":  // авторизация
                sendMessage(BotMessageService.getLoginPromptMessage(chatId));
                setUserAction(chatId, BotUserAction.LOGIN);
                break;

            case "/register":   // регистрация
                sendMessage(BotMessageService.getRegisterPromptMessage(chatId));
                setUserAction(chatId, BotUserAction.REGISTRATION);
                break;

            case "/invitation": // меню WhatsApp приглашений
                if (user.isAuthorize()) {
                    sendMessage(getVacancyMenuForUser(chatId, user));
                    setUserAction(chatId, BotUserAction.INVITATION);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/invitationWithAudio": // меню WhatsApp + Аудио приглашений
                if (user.isAuthorize()) {
                    user.getInvite().setAudioInvite(true);  // устанавливаем признак Аудио-приглашения
                    sendMessage(getVacancyMenuForUser(chatId, user));
                    setUserAction(chatId, BotUserAction.INVITATION);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/invitationWithEmail": // меню WhatsApp + Email приглашений
                if (user.isAuthorize()) {
                    user.getInvite().setEmailInvite(true);  // устанавливаем признак Email-приглашения
                    sendMessage(getVacancyMenuForUser(chatId, user));
                    setUserAction(chatId, BotUserAction.INVITATION);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/invitationWithEmailAndAudio": // меню WhatsApp + Email + Аудио приглашений
                if (user.isAuthorize()) {
                    user.getInvite().setEmailInvite(true);  // устанавливаем признак Email-приглашения
                    user.getInvite().setAudioInvite(true);  // устанавливаем признак Аудио-приглашения
                    sendMessage(getVacancyMenuForUser(chatId, user));
                    setUserAction(chatId, BotUserAction.INVITATION);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/settings":   // переход в меню настроек
                if (user.isAuthorize()) {
                    sendMessage(BotMessageService.getSettingsMenuForUser(chatId, user));
                    setUserAction(chatId, BotUserAction.SETTINGS);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/settingsCompany":  // переход в меню настройки данных компании
                if (user.isAuthorize()) {
                    processCompanyMenuSettings(chatId, user);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/settingsCompany/next":  // обработка нажатия на кнопку "Оставить" в настройках данных компании
                processCompanyMenuSettingsNextButton(chatId, user);
                break;

            case "/settingsUsers":  // переход в меню настройки списка пользователей
                if (user.isAuthorize()) {
                    sendMessage(BotMessageService.getSettingsMenuForUsersList(chatId));
                    setUserAction(chatId, BotUserAction.SETTINGS_USERS);
                } else {
                    processUserNotAuthorized(chatId);
                }
                break;

            case "/users/list": // получение списка пользователей
                sendMessage(getUsersList(chatId, user));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS_LIST);
                break;

            case "/users/add": // добавление нового пользователя - спрашиваем ФИО
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите ФИО пользователя:"));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS_ADD);
                break;

            case "/users/delete": // удаление пользователя - спрашиваем Telegram ID
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите ID пользователя:"));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS_DELETE);
                break;

            case "/users/revert/delete":    // отменить удаление пользователя
                sendMessage(BotMessageService.getSettingsMenuForUsersList(chatId));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS);
                break;

            case "/setupLater":
                // возвращаемся в начальное меню приглашений
                sendMessage(BotMessageService.getInvitationMenuMessage(chatId));
                setUserAction(chatId, BotUserAction.LOGIN);
                break;

            case "/back":
                processBackButtonClick(chatId);
                break;

            case "/logout": // выход из приложения
                processUserLogout(chatId, user);
                break;

            case "/help":   // переход в меню помощи
                sendMessage(BotMessageService.getHelpMenu(chatId));
                sendMessage(BotMessageService.getHelpMenuLinks(chatId));
                setUserAction(chatId, BotUserAction.HELP);
                break;

            default:
                if (callback.startsWith("/vacancy")) {  // обработка выбора вакансии для приглашений
                    processVacancySelect(chatId, user, callback);
                } else if (callback.startsWith("/users/confirm/delete")) {   // обработка подтверждения удаления пользователя
                    processUserDeleteConfirm(chatId, user, callback);
                }
                break;
        }
    }

    /**
     * Отправка сообщения
     *
     * @param message сообщение
     */
    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send message \"{}\" to {} due to error: {}", message.getText(), message.getChatId(), e.getMessage());
        }
    }

    /**
     * Авторизация пользователя в REST API
     *
     * @param user     пользователь
     * @param chatId   идентификатор чата (Telegram ID)
     * @param password пароль
     */
    public void loginUser(BotUser user, long chatId, String password) {
        try {
            if (isNotBlank(password)) {
                UserRole role = httpRequestService.loginUser(chatId, password.trim());
                if (role != null) {
                    user.setRole(role);
                    user.setAuthorize(true);
                    user.setPassword(password);
                    sendMessage(BotMessageService.getInvitationMenuMessage(chatId));
                } else {
                    sendMessage(BotMessageService.getLoginErrorMessage(chatId));
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Получить меню для выбора вакансии
     *
     * @param chatId идентификатор чата
     * @param user   пользователь бота
     * @return сообщение
     */
    public SendMessage getVacancyMenuForUser(long chatId, BotUser user) {
        try {
            List<Vacancy> vacancies = httpRequestService.getVacancyList(user.getTelegramId(), user.getPassword());
            if (!vacancies.isEmpty()) {
                return BotMessageService.getVacancyMenuMessage(chatId, vacancies);
            } else {
                return BotMessageService.getNoVacancyMenuMessage(chatId);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Получить список пользователей для настроек
     *
     * @param chatId идентификатор чата
     * @param user   пользователь бота
     * @return сообщение
     */
    public SendMessage getUsersList(long chatId, BotUser user) {
        try {
            List<User> users = httpRequestService.getUsersList(user.getTelegramId(), user.getPassword());
            if (users != null) {
                return BotMessageService.getUsersListMessage(chatId, users);
            } else {
                return BotMessageService.getSimpleTextMessage(chatId, "Произошла ошибка при получении списка пользователей");
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Установка начального действия для пользователя бота
     *
     * @param chatId идентификатор чата
     */
    private void setStartAction(long chatId) {
        BotUser user = botActionMap.get(chatId);
        if (user != null) {
            user.logoutUser();
            user.setPrevAction(user.getAction());
            user.setAction(BotUserAction.START);
        } else {
            user = new BotUser();
            user.setTelegramId(chatId);
            user.setAction(BotUserAction.START);
            botActionMap.put(chatId, user);
        }
    }

    private void setUserAction(long chatId, BotUserAction action) {
        BotUser user = botActionMap.get(chatId);
        if (user != null) {
            user.setPrevAction(user.getAction());   // запоминаем предыдущее действие пользователя
            user.setAction(action);                 // запоминаем текущее действие пользователя
        }
    }

    /**
     * Обработчик нажатия на кнопку "Обратно" в боте
     *
     * @param chatId идентификатор чата
     */
    private void processBackButtonClick(long chatId) {
        // получаем пользователя и его текущее действие
        BotUser user = botActionMap.get(chatId);
        BotUserAction action = user.getAction();
        switch (action) {
            case INVITATION:    // возвращаемся в начальное меню приглашений
                sendMessage(BotMessageService.getInvitationMenuMessage(chatId));
                setUserAction(chatId, BotUserAction.LOGIN);
                break;
            case REGISTRATION:    // возвращаемся в стартовое меню
                sendMessage(BotMessageService.getStartMenuMessage(chatId));
                setStartAction(chatId);
                break;
            case SETTINGS: // возвращаемся в начальное меню приглашений
                if (user.getPrevAction() != null && user.getPrevAction() == BotUserAction.LOGIN) {
                    sendMessage(BotMessageService.getInvitationMenuMessage(chatId));
                    setUserAction(chatId, BotUserAction.LOGIN);
                } else { // возвращаемся в стартовое меню
                    sendMessage(BotMessageService.getStartMenuMessage(chatId));
                    setStartAction(chatId);
                }
                break;
            case SETTINGS_USERS:    // возвращаемся в главное меню настроек
                sendMessage(BotMessageService.getSettingsMenuForUser(chatId, user));
                setUserAction(chatId, BotUserAction.SETTINGS);
                break;
            case SETTINGS_USERS_LIST:    // возвращаемся в меню настроек пользователей
                sendMessage(BotMessageService.getSettingsMenuForUsersList(chatId));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS);
                break;
            case HELP:    // возвращаемся в стартовое меню
                sendMessage(BotMessageService.getStartMenuMessage(chatId));
                setStartAction(chatId);
                break;
            default:
                break;
        }
    }

    /**
     * Обработчик нажатия на кнопку "Оставить"
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     */
    private void processCompanyMenuSettingsNextButton(long chatId, BotUser user) {
        BotUserAction action = user.getAction();
        switch (action) {
            case SETTINGS_COMPANY_FIO:
                sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Телефон", user.getSettings().getPhone()));
                setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_PHONE);
                break;
            case SETTINGS_COMPANY_PHONE:
                sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Сайт", user.getSettings().getSite()));
                setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_SITE);
                break;
            case SETTINGS_COMPANY_SITE:
                sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Адрес", user.getSettings().getAddress()));
                setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_ADDRESS);
                break;
            case SETTINGS_COMPANY_ADDRESS:
                sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Название компании", user.getSettings().getCompanyName()));
                setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_NAME);
                break;
            case SETTINGS_COMPANY_NAME:
                sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Описание компании", user.getSettings().getCompanyDescription()));
                setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_DESCRIPTION);
                break;
            case SETTINGS_COMPANY_DESCRIPTION:
                processSettingsCompanyDescriptionInput(chatId, user, null);
                break;
            default:
                break;
        }
    }

    /**
     * Обработчик выбора вакансии для отправки приглашений
     *
     * @param chatId   идентификатор чата
     * @param user     пользователь Telegram
     * @param callback callback с идентификатором вакансии
     */
    private void processVacancySelect(long chatId, BotUser user, String callback) {
        // получаем и устанавливаем идентификатор вакансии
        Long vacancyId = Long.parseLong(callback.substring(callback.lastIndexOf('/') + 1));
        user.setVacancyId(vacancyId);
        // запрашиваем имя пользователя для приглашения
        sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите имя пользователя:"));
        setUserAction(chatId, BotUserAction.INVITATION_VACANCY);
    }

    /**
     * Обработка ввода имени пользователя для приглашения
     *
     * @param chatId   идентификатор чата
     * @param user     пользователь Telegram
     * @param userName имя пользователя для приглашения
     */
    private void processInvitationUserNameInput(long chatId, BotUser user, String userName) {
        // заполняем введенное имя пользователя для приглашения
        user.getInvite().setName(userName);
        // запрашиваем имя пользователя для приглашения
        sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите телефон пользователя:"));
        setUserAction(chatId, BotUserAction.INVITATION_USER_NAME);
    }

    /**
     * Обработка ввода номера телефона пользователя для приглашения
     *
     * @param chatId    идентификатор чата
     * @param user      пользователь Telegram
     * @param userPhone номер телефона пользователя для приглашения
     */
    private void processInvitationUserPhoneInput(long chatId, BotUser user, String userPhone) {
        try {
            if (PhoneUtils.isOnlyDigitsInPhone(userPhone) && PhoneUtils.isPhoneMobile(userPhone)) {
                // заполняем введенный номер телефона пользователя для приглашения
                user.getInvite().setPhone(userPhone);
                setUserAction(chatId, BotUserAction.INVITATION_USER_PHONE);

                if (user.getInvite().isEmailInvite()) { // запрашиваем дополнительно Email
                    sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите Email пользователя:"));
                } else {    // отправляем приглашение пользователю
                    sendUserInvitation(chatId, user);
                }
            } else {    // повторно запрашиваем телефон
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Неверное значение для телефона пользователя!"));
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите телефон пользователя:"));
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Обработка ввода Email пользователя для приглашения
     *
     * @param chatId    идентификатор чата
     * @param user      пользователь Telegram
     * @param userEmail имейл пользователя для приглашения
     */
    private void processInvitationUserEmailInput(long chatId, BotUser user, String userEmail) {
        try {
            if (EmailUtils.isValid(userEmail)) {
                // заполняем введенный Email пользователя для приглашения
                user.getInvite().setEmail(userEmail);
                setUserAction(chatId, BotUserAction.INVITATION_USER_EMAIL);
                // отправляем приглашение пользователю
                sendUserInvitation(chatId, user);
            } else {    // повторно запрашиваем телефон
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Неверное значение для Email пользователя!"));
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите Email пользователя:"));
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Отправка приглашений
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     */
    private void sendUserInvitation(long chatId, BotUser user) {
        try {
            // отправляем приглашения пользователю
            boolean invitationResult = httpRequestService.inviteUser(user.getTelegramId(),
                    user.getPassword(),
                    user.getVacancyId(),
                    user.getInvite());
            if (invitationResult) {
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Приглашение успешно отправлено"));
            } else {
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Возникла ошибка при отправке приглашения"));
            }
            // возвращаемся в начальное меню приглашений
            sendMessage(BotMessageService.getInvitationMenuMessage(chatId));
            setUserAction(chatId, BotUserAction.LOGIN);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Выход пользователя в стартовое меню
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     */
    private void processUserLogout(Long chatId, BotUser user) {
        // отменяем авторизацию для пользователя
        user.logoutUser();
        // переходим в стартовое меню
        sendMessage(BotMessageService.getStartMenuMessage(chatId));
        setStartAction(chatId);
    }

    /**
     * Обработка отсутствия авторизации у пользователя
     *
     * @param chatId идентификатор чата
     */
    private void processUserNotAuthorized(Long chatId) {
        sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Вы не авторизованы!"));
        // переходим в стартовое меню
        sendMessage(BotMessageService.getStartMenuMessage(chatId));
        setStartAction(chatId);
    }

    /**
     * Обработка ввода имени пользователя для регистрации
     *
     * @param chatId   идентификатор чата
     * @param user     пользователь Telegram
     * @param userName имя регистрируемого пользователя
     */
    private void processRegistrationUserNameInput(long chatId, BotUser user, String userName) {
        // заполняем введенное имя пользователя для регистрации
        user.getRegistration().setName(userName);
        user.getRegistration().setSocialUserId(String.valueOf(user.getTelegramId()));
        // запрашиваем Email для регистрации
        sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите Ваш E-mail:"));
        setUserAction(chatId, BotUserAction.REGISTRATION_USER_NAME);
    }

    /**
     * Обработка ввода Email пользователя для регистрации
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     * @param email  email регистрируемого пользователя
     */
    private void processRegistrationUserEmailInput(long chatId, BotUser user, String email) {
        if (EmailUtils.isValid(email)) {
            // заполняем введенный Email пользователя для регистрации
            user.getRegistration().setEmail(email);
            // запрашиваем пароль для регистрации
            sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите Ваш пароль:"));
            setUserAction(chatId, BotUserAction.REGISTRATION_USER_EMAIL);
        } else {    // повторно запрашиваем email
            sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Неверное значение для E-mail!"));
            sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Введите Ваш E-mail:"));
        }
    }

    /**
     * Обработка ввода пароля пользователя для регистрации
     *
     * @param chatId   идентификатор чата
     * @param user     пользователь Telegram
     * @param password пароль регистрируемого пользователя
     */
    private void processRegistrationUserPasswordInput(long chatId, BotUser user, String password) {
        try {
            // заполняем введенный пароль пользователя для регистрации
            user.getRegistration().setPassword(password);
            user.getRegistration().setConfirmPassword(password);
            setUserAction(chatId, BotUserAction.REGISTRATION_USER_PASSWORD);
            // регистрируем нового пользователя
            RegistrationResult result = httpRequestService.registerUser(user.getRegistration());
            if (result.success) {   // регистрация успешна - авторизуем и показываем пароль пользователя
                user.setAuthorize(true);
                user.setPassword(password);
                user.setRole(UserRole.OWNER);

                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Регистрация успешна. Ваш пароль: " + password));
                sendMessage(BotMessageService.getRegistrationSettingsMenu(chatId));
            } else {    // регистрация неуспешна - переход в стартовое меню
                sendMessage(BotMessageService.getSimpleTextMessage(chatId,
                        "Ошибка при регистрации" + (result.message != null ? ": " + result.message : "")));
                sendMessage(BotMessageService.getStartMenuMessage(chatId));
                setStartAction(chatId);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Добавление нового пользователя
     *
     * @param chatId   идентификатор чата
     * @param user     пользователь Telegram
     * @param userName имя нового пользователя
     */
    private void processAddNewUser(long chatId, BotUser user, String userName) {
        try {
            User u = httpRequestService.addNewUser(user.getTelegramId(), user.getPassword(), userName);
            if (u != null) {
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Пользователь " + userName + " добавлен. Его пароль: " + u.getPassword()));
            } else {
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Не удалось создать пользователя"));
            }
            sendMessage(BotMessageService.getSettingsMenuForUsersList(chatId));
            setUserAction(chatId, BotUserAction.SETTINGS_USERS);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Обработка ввода ID при удалении пользователя
     *
     * @param chatId       идентификатор чата
     * @param user         пользователь Telegram
     * @param socialUserId ID удаляемого пользователя
     */
    private void processUserIdInput(long chatId, BotUser user, String socialUserId) {
        try {
            // получаем список пользователей
            List<User> users = httpRequestService.getUsersList(user.getTelegramId(), user.getPassword());
            User findUser = users
                    .stream()
                    .filter(u -> u.getSocialUserId().equals(socialUserId))
                    .findAny()
                    .orElse(null);
            if (findUser != null) { // пользователь найден - запрос на подтверждение удаления
                sendMessage(BotMessageService.getDeleteUserConfirmationMenu(chatId, findUser));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS_DELETE_CONFIRM);
            } else {
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Не найден пользователь с ID: " + socialUserId));
                sendMessage(BotMessageService.getSettingsMenuForUsersList(chatId));
                setUserAction(chatId, BotUserAction.SETTINGS_USERS);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Подтверждение удаления пользователя
     *
     * @param chatId   идентификатор чата
     * @param user     пользователь Telegram
     * @param callback коллбэк
     */
    private void processUserDeleteConfirm(long chatId, BotUser user, String callback) {
        try {
            // получаем ID пользователя из коллбэка
            String socialUserId = callback.substring(callback.lastIndexOf('/') + 1);
            boolean result = httpRequestService.deleteUser(user.getTelegramId(), user.getPassword(), socialUserId);
            if (result) {   // удаление успешно
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Пользователь с ID " + socialUserId + " удален"));
            } else {    // удаление не успешно
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Ошибка при удалении пользователя с ID " + socialUserId));
            }
            sendMessage(BotMessageService.getSettingsMenuForUsersList(chatId));
            setUserAction(chatId, BotUserAction.SETTINGS_USERS);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Обработка начала ввода данных по компании
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     */
    private void processCompanyMenuSettings(long chatId, BotUser user) {
        try {
            // получаем текущие настройки
            user.setSettings(httpRequestService.getAccountSettings(user.getTelegramId(), user.getPassword()));
            sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "ФИО менеджера", user.getSettings().getManagerName()));
            setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_FIO);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Обработка ввода ФИО менеджера для данных компании
     *
     * @param chatId      идентификатор чата
     * @param user        пользователь Telegram
     * @param managerName ФИО менеджера
     */
    private void processSettingsCompanyFioInput(long chatId, BotUser user, String managerName) {
        user.getSettings().setManagerName(managerName);
        // показываем следующее поле
        sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Телефон", user.getSettings().getPhone()));
        setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_PHONE);
    }

    /**
     * Обработка ввода телефона для данных компании
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     * @param phone  телефон
     */
    private void processSettingsCompanyPhoneInput(long chatId, BotUser user, String phone) {
        if (PhoneUtils.isOnlyDigitsInPhone(phone) && PhoneUtils.isPhoneMobile(phone)) {
            user.getSettings().setPhone(phone);
            // показываем следующее поле
            sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Сайт", user.getSettings().getSite()));
            setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_SITE);
        } else {    // повторно запрашиваем телефон
            sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Неверное значение для телефона компании!"));
            sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Телефон", user.getSettings().getPhone()));
        }
    }

    /**
     * Обработка ввода сайта для данных компании
     *
     * @param chatId идентификатор чата
     * @param user   пользователь Telegram
     * @param site   сайт
     */
    private void processSettingsCompanySiteInput(long chatId, BotUser user, String site) {
        user.getSettings().setSite(site);
        // показываем следующее поле
        sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Адрес", user.getSettings().getAddress()));
        setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_ADDRESS);
    }

    /**
     * Обработка ввода адреса для данных компании
     *
     * @param chatId  идентификатор чата
     * @param user    пользователь Telegram
     * @param address адрес компании
     */
    private void processSettingsCompanyAddressInput(long chatId, BotUser user, String address) {
        user.getSettings().setAddress(address);
        // показываем следующее поле
        sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Название компании", user.getSettings().getCompanyName()));
        setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_NAME);
    }

    /**
     * Обработка ввода названия компании для данных компании
     *
     * @param chatId      идентификатор чата
     * @param user        пользователь Telegram
     * @param companyName название компании
     */
    private void processSettingsCompanyNameInput(long chatId, BotUser user, String companyName) {
        user.getSettings().setCompanyName(companyName);
        // показываем следующее поле
        sendMessage(BotMessageService.getSettingsMenuForCompany(chatId, "Описание компании", user.getSettings().getCompanyDescription()));
        setUserAction(chatId, BotUserAction.SETTINGS_COMPANY_DESCRIPTION);
    }

    /**
     * Обработка ввода описания компании для данных компании
     *
     * @param chatId             идентификатор чата
     * @param user               пользователь Telegram
     * @param companyDescription описание компании
     */
    private void processSettingsCompanyDescriptionInput(long chatId, BotUser user, String companyDescription) {
        try {
            if (isNotBlank(companyDescription)) {
                user.getSettings().setCompanyDescription(companyDescription);
            }
            // сохраняем настройки
            boolean result = httpRequestService.saveAccountSettings(user.getSettings(), user.getTelegramId(), user.getPassword());
            if (result) {   // сохранение успешно
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Настройки успешно сохранены"));
            } else {    // сохранение не успешно
                sendMessage(BotMessageService.getSimpleTextMessage(chatId, "Ошибка при сохранении настроек"));
            }
            // показываем основное меню настроек
            sendMessage(BotMessageService.getSettingsMenuForUser(chatId, user));
            setUserAction(chatId, BotUserAction.SETTINGS);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
}
