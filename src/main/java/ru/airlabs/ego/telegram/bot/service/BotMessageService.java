package ru.airlabs.ego.telegram.bot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.airlabs.ego.telegram.bot.model.BotUser;
import ru.airlabs.ego.telegram.bot.model.User;
import ru.airlabs.ego.telegram.bot.model.UserRole;
import ru.airlabs.ego.telegram.bot.model.Vacancy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Вспомогательный класс для формирования сообщений для Telegram-бота
 *
 * @author Aleksey Gorbachev
 */
public class BotMessageService {

    /**
     * Формирование сообщения для стартового меню
     *
     * @param chatId идентификатор чата
     * @return сообщение для формирования стартового меню
     */
    public static SendMessage getStartMenuMessage(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Выберите действие:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Вход").setCallbackData("/login"));
        rowInline.add(new InlineKeyboardButton().setText("Регистрация").setCallbackData("/register"));
        rowInline.add(new InlineKeyboardButton().setText("Помощь").setCallbackData("/help"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для меню приглашений
     *
     * @param chatId идентификатор чата
     * @return сообщение для формирования меню приглашений
     */
    public static SendMessage getInvitationMenuMessage(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Выберите действие:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline4 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline5 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Пригласить по WhatsApp").setCallbackData("/invitation"));
        rowInline2.add(new InlineKeyboardButton().setText("Пригласить по WhatsApp+Звонок").setCallbackData("/invitationWithAudio"));
        rowInline3.add(new InlineKeyboardButton().setText("Пригласить по WhatsApp+Email").setCallbackData("/invitationWithEmail"));
        rowInline4.add(new InlineKeyboardButton().setText("Пригласить по WhatsApp+Email+Звонок").setCallbackData("/invitationWithEmailAndAudio"));
        rowInline5.add(new InlineKeyboardButton().setText("Настройки").setCallbackData("/settings"));
        rowInline5.add(new InlineKeyboardButton().setText("Выход").setCallbackData("/logout"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        rowsInline.add(rowInline4);
        rowsInline.add(rowInline5);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для меню выбора вакансии
     *
     * @param chatId    идентификатор чата
     * @param vacancies список вакансий
     * @return сообщение для меню выбора вакансии
     */
    public static SendMessage getVacancyMenuMessage(long chatId, List<Vacancy> vacancies) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Выберите вакансию:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Vacancy vacancy : vacancies) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(vacancy.getName()).setCallbackData("/vacancy/" + vacancy.getId()));
            rowsInline.add(rowInline);
        }
        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        rowsInline.add(backRow);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для пустого списка вакансий
     *
     * @param chatId идентификатор чата
     * @return сообщение для пустого списка вакансий
     */
    public static SendMessage getNoVacancyMenuMessage(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Нет вакансий");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для вывода списка пользователей
     *
     * @param chatId идентификатор чата
     * @param users  список пользователей
     * @return сообщение для списка пользователей
     */
    public static SendMessage getUsersListMessage(long chatId, List<User> users) {
        String messageText;
        if (!users.isEmpty()) {
            messageText = users.stream().map(User::toString).collect(Collectors.joining("\n"));
        } else {
            messageText = "Нет пользователей";
        }
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText(messageText);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для регистрации
     *
     * @param chatId идентификатор чата
     * @return сообщение для регистрации
     */
    public static SendMessage getRegisterPromptMessage(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Введите имя:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для настройки после регистрации
     *
     * @param chatId идентификатор чата
     * @return сообщение для настройки после регистрации
     */
    public static SendMessage getRegistrationSettingsMenu(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Выберите действие:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Настроить сейчас").setCallbackData("/settings"));
        rowInline.add(new InlineKeyboardButton().setText("Настроить позже").setCallbackData("/setupLater"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для меню помощи
     *
     * @param chatId идентификатор чата
     * @return сообщение для формирования меню помощи
     */
    public static SendMessage getHelpMenu(long chatId) {
        SendMessage message = new SendMessage()
                .setText("Выберите действие: ")
                .setChatId(chatId);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Документация").setUrl("https://telegram.org/faq"));
        rowInline2.add(new InlineKeyboardButton().setText("Viber поддержка").setUrl("https://support.viber.com/customer/ru/portal/articles"));
        rowInline3.add(new InlineKeyboardButton().setText("WhatsApp поддержка").setUrl("https://faq.whatsapp.com/?lang=ru"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для отображения ссылок в меню помощи
     *
     * @param chatId идентификатор чата
     * @return сообщение для формирования меню помощи
     */
    public static SendMessage getHelpMenuLinks(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Позвонить в поддержку: +79119119111 \n" + "Написать: support@help.com");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для меню настроек
     *
     * @param chatId идентификатор чата
     * @param user   пользователь
     * @return сообщение для меню настроек
     */
    public static SendMessage getSettingsMenuForUser(long chatId, BotUser user) {
        SendMessage message = new SendMessage()
                .setText("Меню настроек:")
                .setChatId(chatId);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline4 = new ArrayList<>();
        if (user.getRole() == UserRole.OWNER) {
            rowInline1.add(new InlineKeyboardButton().setText("Данные компании").setCallbackData("/settingsCompany"));
            rowInline1.add(new InlineKeyboardButton().setText("Список сотрудников").setCallbackData("/settingsUsers"));
        }
        //rowInline2.add(new InlineKeyboardButton().setText("Настроить приглашения").setCallbackData("settingsInvitation"));
        //rowInline3.add(new InlineKeyboardButton().setText("Оплата").setCallbackData("/pay"));
        rowInline4.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        rowsInline.add(rowInline4);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование меню настроек для настройки списка пользователей
     *
     * @param chatId идентификатор чата
     * @return сообщение для меню настроек списка пользователей
     */
    public static SendMessage getSettingsMenuForUsersList(long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Выберите действие:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Показать список").setCallbackData("/users/list"));
        rowInline1.add(new InlineKeyboardButton().setText("Добавить").setCallbackData("/users/add"));
        rowInline2.add(new InlineKeyboardButton().setText("Удалить").setCallbackData("/users/delete"));
        rowInline2.add(new InlineKeyboardButton().setText("<- Обратно").setCallbackData("/back"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование меню для подтверждения удаления пользователя
     *
     * @param chatId идентификатор чата
     * @param user   удаляемый пользователь
     * @return сообщение для меню для подтверждения удаления пользователя
     */
    public static SendMessage getDeleteUserConfirmationMenu(long chatId, User user) {
        SendMessage message = new SendMessage()
                .setText("Удалить пользователя " + user.getName() + "?")
                .setChatId(chatId);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Да").setCallbackData("/users/confirm/delete/" + user.getSocialUserId()));
        rowInline.add(new InlineKeyboardButton().setText("Нет").setCallbackData("/users/revert/delete"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;

    }

    /**
     * Формирование сообщения для ввода данных по компании и личных данных
     *
     * @param chatId    идентификатор чата
     * @param fieldName название поля
     * @param value     текущее значение поля
     * @return сообщение
     */
    public static SendMessage getSettingsMenuForCompany(long chatId, String fieldName, String value) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText("Текущее значение для поля '" + fieldName + "': " + (isNotBlank(value) ? value : "Поле не заполнено") + "\n" +
                        "Оставьте текущее значение, или введите новое:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Оставить").setCallbackData("/settingsCompany/next"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    /**
     * Формирование сообщения для авторизации
     *
     * @param chatId идентификатор чата
     * @return сообщение для авторизации
     */
    public static SendMessage getLoginPromptMessage(long chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText("Для авторизации введите Ваш пароль:");
    }

    /**
     * Формирование сообщения об ошибке ввода
     *
     * @param chatId идентификатор чата
     * @return сообщение
     */
    public static SendMessage getLoginErrorMessage(long chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText("Введены неверные данные");
    }

    /**
     * Формирование сообщения с заданным текстом
     *
     * @param chatId идентификатор чата
     * @param text   текст сообщения
     * @return сообщение
     */
    public static SendMessage getSimpleTextMessage(long chatId, String text) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(text);
    }
}
