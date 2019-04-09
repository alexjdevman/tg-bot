package ru.airlabs.ego.telegram.bot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Тестовый REST-контроллер
 */
@RestController
@RequestMapping("/bot")
public class IndexController {

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @ResponseBody
    public String bot() {
        return "Telegram Bot Application";
    }

}
