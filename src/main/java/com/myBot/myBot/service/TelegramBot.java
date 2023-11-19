package com.myBot.myBot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBot.myBot.config.BotConfig;
import com.myBot.myBot.config.ImageGenerator;
import com.myBot.myBot.config.KeyboardSetter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Slf4j
@Component
public final class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    private ArrayList<Update> lastUpdates = new ArrayList<>(2);
    private String prompt;



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





    private boolean checkIfTextWas(int position, String text) {
        return lastUpdates.get(position) != null && lastUpdates.get(position).getMessage().getText().contains(text);
    }
    @Override
    public void onUpdateReceived(Update update) {
        updateReceived(update);
        checkPromptAndGenerate();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            //acts depending on a command received
            switch (messageText) {
                case "/start" -> startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                case "/photo" -> sendMessage(chatId, "Pick a prompt, " +
                        "let's start with the age of a girl you want to get generated.", KeyboardSetter.ageMenu());
                case "/help" -> sendMessage(chatId, HELP_TEXT, null);
            }
        }

    }


    //The method checks if user follows the flow of building a prompt, the flow consists of different questions
    //asked by bot, such as "pick a breast size", and user is supposed to pick one of the options
    //on the Screen Keyboard (KeyboardSetter class configures those keyboards, and they are set with each
    // sendMessage method call). If user doesn't pick one of the suggested prompts or writes a prompt without
    //following the flow, user is asked to start generating a prompt anew. After successful prompt build sendMessage
    //method is invoked and a final prompt is given as a parameter.

    private void checkPromptAndGenerate() {

        if (lastUpdates.get(0) != null) {
            if (checkIfTextWas(1, "yo")) { //checks if the text entered is actually one of the suggested prompts
                if (checkIfTextWas(0, "/photo")) { //checks if one of the suggested prompts was entered after a command /photo
                    prompt = "fully naked girl, real, masterpiece, best quality, " +
                            "(detail skin texture, ultra-detailed body)" + lastUpdates.get(1).getMessage().getText(); //prompt is updated
                    sendMessage(lastUpdates.get(1).getMessage().getChatId(), "Okay, now let's pick a type of body.", KeyboardSetter.bodyMenu());
                } else {
                    sendMessage(lastUpdates.get(1).getMessage().getChatId(), "You should enter command " +
                            "/photo before.", KeyboardSetter.startMenu());
                }
            } else if (!checkIfTextWas(1, "yo") && checkIfTextWas(0, "/photo")) {
                //if after command /photo user didn't pick on of the suggested prompts
                sendMessage(lastUpdates.get(1).getMessage().getChatId(), "You should have picked " +
                        "one of the suggested prompts, please, start again.", KeyboardSetter.startMenu());
            }

            if (checkIfTextWas(1, "body")) {
                if (checkIfTextWas(0, "yo")) {
                    String lastPrompt = prompt;
                    String newPrompt = lastUpdates.get(1).getMessage().getText();
                    prompt = lastPrompt + ", " + newPrompt + ", ";
                    sendMessage(lastUpdates.get(1).getMessage().getChatId(), "It's time to decide what kind" +
                            " of boobs should she have.", KeyboardSetter.boobsMenu());
                } else {
                    sendMessage(lastUpdates.get(1).getMessage().getChatId(), "You should follow along, try " +
                            "generating one more time starting from the command /photo.", KeyboardSetter.startMenu());
                }
            } else if (!checkIfTextWas(1, "body") && checkIfTextWas(0, "yo")) {
                sendMessage(lastUpdates.get(1).getMessage().getChatId(), "This option is not provided, " +
                        "try again", KeyboardSetter.startMenu());
            }

            if (checkIfTextWas(1,"breast")) {
                if (checkIfTextWas(0,"body")) {
                    String lastPrompt = prompt;
                    String newPrompt = lastUpdates.get(1).getMessage().getText();
                    prompt = lastPrompt + newPrompt + " ";
                    sendMessage(lastUpdates.get(1).getMessage().getChatId(), "What color should the hair be?", KeyboardSetter.hairMenu());
                    try {
                        sendImage(lastUpdates.get(1).getMessage().getChatId(), prompt);
                        log.info("sendImage method called " + prompt);
                    } catch (IOException e) {
                        log.error("Error occurred when calling sendImage method: " + e.getMessage());
                    }
                } else {
                    sendMessage(lastUpdates.get(1).getMessage().getChatId(), "You should follow along, try " +
                            "generating one more time starting from the command /photo.", KeyboardSetter.startMenu());
                }
            } else if (!checkIfTextWas(1,"tits") && checkIfTextWas(0,"body")) {
                sendMessage(lastUpdates.get(1).getMessage().getChatId(), "This option is not provided, " +
                        "try again", KeyboardSetter.startMenu());
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

    private void sendMessage (long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
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
            saveAsPng(decodedBytes);

            SendPhoto photo = new SendPhoto();
            InputFile photoFile = new InputFile(new File("output.png"));
            photo.setPhoto(photoFile);
            photo.setChatId(chatId);

            try {
                sendMessage(chatId, "Here is your image:", KeyboardSetter.startMenu());
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
    private static void saveAsPng(byte[] imageBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("output.png")) {
            fos.write(imageBytes);
            log.info("image saved");
        }
    }
}
