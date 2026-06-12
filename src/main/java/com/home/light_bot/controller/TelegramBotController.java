package com.home.light_bot.controller;

import com.home.light_bot.dto.ResponseGetCurrentVoltageDto;
import com.home.light_bot.service.TuyaApiService;
import com.home.light_bot.service.TuyaAuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TelegramBotController extends TelegramLongPollingBot {
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.name}")
    private String botName;

    private final TuyaApiService tuyaApiService;
    private final TuyaAuthService tuyaAuthService;

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        String messageId = "msg_id-" + UUID.randomUUID().toString().substring(0,32);
        MDC.put("messageId", messageId);
        try {
            if(update.hasMessage() && update.getMessage().hasText()) {
                Message inMessage = update.getMessage();
                String chatId = inMessage.getChatId().toString();
                String userMessage = inMessage.getText();

                log.info("Get command '{}' from client with chatId: {}", userMessage, chatId);

                if (userMessage.equals("/getToken")) {
                    String token = tuyaAuthService.getToken();
                    SendMessage messageToExecute = new SendMessage(
                            chatId,
                            token
                    );
                    execute(messageToExecute);

                } else if (userMessage.equals("/getVoltage")){
                    ResponseGetCurrentVoltageDto response  = tuyaApiService.getCurrentVoltage();
                    SendMessage messageToExecute = new SendMessage(
                            chatId,
                            response.getCurrentVoltage().toString()
                    );
                    execute(messageToExecute);
                }
            }
        } catch (Exception ex){
            log.error("Something wrong into bot exception: {}", ex.getMessage());
        } finally {
            MDC.clear();
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
