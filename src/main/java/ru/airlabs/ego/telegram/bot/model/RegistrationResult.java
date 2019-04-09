package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для результата регистрации пользователя
 *
 * @author Aleksey Gorbachev
 */
public class RegistrationResult {

    /**
     * Признак успешности регистрации
     */
    public boolean success = false;

    /**
     * Сообщение об ошибке
     */
    public String message;
}
