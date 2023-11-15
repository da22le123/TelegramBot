package com.myBot.myBot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBot.myBot.config.BotConfig;
import com.myBot.myBot.config.ImageGenerator;
import com.myBot.myBot.config.KeyboardSetter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    private ArrayList<Update> lastUpdates = new ArrayList<>(2);



    private static final String HELP_TEXT =
            """
                    This bot allows you to generate images depending on your prompt. There are some rules to follow, and advices to comply with:\s
                    1) Write you prompt in ENGLISH language
                    2) Do not make it longer than 15 words\s
                    3) Try not to include complicated phrases and make sure your prompt is grammatically correct!""" + "\n" +
                    "List of commands: " + "\n" +
                    "1) /start - receive welcoming message" + "\n" +
                    "2) /help - get information about bot's usage" + "\n" +
                    "3) /settings" + "\n" +
                    "4) /my_data" + "\n" +
                    "5) /photo - start generating a prompt based photo" + "\n";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start the bot"));
        listOfCommands.add(new BotCommand("/help", "info how to use"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));
        listOfCommands.add(new BotCommand("/my_data", "start the bot"));
        listOfCommands.add(new BotCommand("/photo", "generate photo"));


        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));

        }
        catch (TelegramApiException e) {
                log.error("Error setting bot's command list: " + e.getMessage());
        }
        lastUpdates.add(null);
        lastUpdates.add(null);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }





    private boolean checkIfTextIs(String command) {
        return lastUpdates.get(1) != null && lastUpdates.get(1).getMessage().getText().contains(command);
    }
    @Override
    public void onUpdateReceived(Update update) {
        updateReceived(update);


    if (checkIfTextIs("of")) { //checks if the text entered is actually one of the suggested prompts
        if (lastUpdates.get(0).getMessage().getText().contains("/photo")) {
            String prompt = "cup " + lastUpdates.get(1).getMessage().getText();
            try {
                sendImage(lastUpdates.get(1).getMessage().getChatId(), prompt);
                log.info("sendImage method called");
            } catch (IOException e) {
                log.error("Error occurred when calling sendImage method: " + e.getMessage());
            }
        } else {
            sendMessage(lastUpdates.get(1).getMessage().getChatId(), "This option is not provided", null );
        }
    }






        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            //acts depending on a command received
            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());

                    break;
                case "/photo":
                    sendMessage(chatId, "Pick a prompt", KeyboardSetter.promptMenu());

                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT, null);

                    break;
            }
        }

    }
    
    //updates list of updates because method onUpdateReceived doesn't know which update was later or sooner
    //it doesn't have the history of updates. In order to get previous update, I need this method.
    //Max capacity is 2 so not to store more than 2 updates, when we receive an update, firstly we
    //move element(1) to index 0, than add an update to index 1
    
    private void updateReceived(Update update) {
        lastUpdates.add(0, lastUpdates.get(1));
        lastUpdates.add(1, update);
    }

    private void startCommandReceived(long chatId, String name) {
    String answer = "Привет, " + name + ", приятно познакомиться! Type /help to get information about this bot.";
    log.info("Replied to user " + name);

    sendMessage(chatId, answer, KeyboardSetter.startMenu());
    }

    public void sendMessage (long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        if (keyboardMarkup != null) {
            message.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendImage(long chatId, String prompt) throws IOException {
        ImageGenerator imageGenerator = new ImageGenerator();
        imageGenerator.setPrompt(prompt);
        String imgBase64;
        try {
            imgBase64 = getImageOutJson(imageGenerator.sendRequest());
            byte[] decodedBytes = Base64.getDecoder().decode(imgBase64);
            saveAsPng(decodedBytes, "output.png");

            SendPhoto photo = new SendPhoto();
            InputFile photoFile = new InputFile(new File("output.png"));
            photo.setPhoto(photoFile);
            photo.setChatId(chatId);

            try {
                execute(photo);
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }

        } catch (IOException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
    public static String getImageOutJson (Response response) throws IOException {
        String base64img;
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(response.body().string());
        base64img = rootNode.get("image").asText();

        return base64img;
    }
    private static void saveAsPng(byte[] imageBytes, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(imageBytes);
            log.info("image saved");
        }
    }
}
