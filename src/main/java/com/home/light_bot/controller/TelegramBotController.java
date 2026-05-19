package com.home.light_bot.controller;

import com.home.light_bot.service.TuyaService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TelegramBotController extends TelegramLongPollingBot {
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.name}")
    private String botName;

    private final TuyaService tuyaService;

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            Message inMessage = update.getMessage();
            String chatId = inMessage.getChatId().toString();
            String userMessage = inMessage.getText();
            if (userMessage.equals("/getToken")) {
                String token = tuyaService.getToken();
                SendMessage messageToExecute = new SendMessage(
                        chatId,
                        token
                );
                execute(messageToExecute);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
