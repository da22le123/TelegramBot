package com.myBot.myBot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBot.myBot.config.BotConfig;
import com.myBot.myBot.config.ImageGenerator;
import com.myBot.myBot.config.KeyboardSetter;
import com.myBot.myBot.module.UpdateRepository;
import com.myBot.myBot.module.User;
import com.myBot.myBot.module.UserRepository;
import com.myBot.myBot.module.UserUpdate;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


@Slf4j
@Component
public final class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UpdateRepository updateRepository;
    final BotConfig config;
    private String prompt;
    private String modelId;

    private static final String START_TEXT =
            "Hello, you just started the bot!" +
            " Type /help to get information about this bot.";
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
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateReceived(update);
        checkPromptAndGenerate(update.getMessage().getChatId());

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> {
                    registerUser(update.getMessage());
                    sendMessage(chatId, START_TEXT, KeyboardSetter.startMenu());
                }
                case "/photo" ->
                        sendMessage(chatId, "Pick a prompt, let's start with the style of picture. All of the options are displayed in the Screen Keyboard.",
                                KeyboardSetter.styleMenu());
                case "/help" -> sendMessage(chatId, HELP_TEXT, KeyboardSetter.startMenu());
            }
        }

    }




    //########################################## PROMPT SECTION ######################################################

    //The method checks if user follows the flow of building a prompt, the flow consists of different questions
    //asked by bot, such as "pick a breast size", and user is supposed to pick one of the options
    //on the Screen Keyboard (KeyboardSetter class configures those keyboards, and they are set with each
    // sendMessage method call). If user doesn't pick one of the suggested prompts or writes a prompt without
    //following the flow, user is asked to start generating a prompt anew. After successful prompt build sendMessage
    //method is invoked and a final prompt is given as a parameter.

    private void checkPromptAndGenerate(Long chatId) {
            if (checkAndSetStyle(chatId)) {
                sendMessage(chatId, "Okay, now let's pick an age of a girl you'd like to generate.", KeyboardSetter.ageMenu());
            }
            if (checkAndSetAgePrompt(chatId)) {
                sendMessage(chatId, "Okay, now let's pick a type of body.", KeyboardSetter.bodyMenu());
            }
            if (checkAndSetBodyPrompt(chatId)){
                sendMessage(chatId, "It's time to decide what kind of boobs should she have.", KeyboardSetter.boobsMenu());
            }
            if (checkAndSetBreastPrompt(chatId)) {
                sendMessage(chatId, "What color should the hair be?", KeyboardSetter.hairMenu());

                try {
                    sendImage(chatId, prompt, modelId);
                } catch (IOException e) {
                    log.error("Error occurred when calling sendImage method: " + e.getMessage());
                }
            }
    }

    private boolean checkAndSetStyle(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "/photo")) {
            if (checkIfUpdateContains(1, chatId, "Anime style")) {
                modelId = "dark-sushi-mix-v2-25";
                return true;
            } else if (checkIfUpdateContains(1, chatId, "Realistic style")) {
                modelId = "realistic-vision-v5-1";
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetAgePrompt(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "style")) {
            if (checkIfUpdateContains(1, chatId, "yo")) {
                prompt = "fully naked girl, real, masterpiece, best quality, " +
                        "(detail skin texture, ultra-detailed body)" + getUpdateOnPosition(1, chatId);
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetBodyPrompt(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "yo")) {
            if (checkIfUpdateContains(1, chatId, "body")) {
                String lastPrompt = prompt;
                String newPrompt = getUpdateOnPosition(1, chatId);
                prompt = lastPrompt + newPrompt + " ";
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetBreastPrompt(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "body")) {
            if (checkIfUpdateContains(1, chatId, "breast")) {
            String lastPrompt = prompt;
            String newPrompt = getUpdateOnPosition(1, chatId);
            prompt = lastPrompt + newPrompt + " ";
            return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private void startAllOver(Long chatId) {
        sendMessage(chatId, "You should follow along and only pick one of the suggested prompts, try " +
                "generating one more time starting from the command /photo.", KeyboardSetter.startMenu());
    }

    //########################################## PROMPT SECTION ######################################################



    //########################################## IMAGE SECTION #######################################################

    private void sendImage(long chatId, String prompt, String modelId) throws IOException {
        ImageGenerator imageGenerator = new ImageGenerator();
        imageGenerator.setPrompt(prompt);
        imageGenerator.setModelId(modelId);
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

    private static String getImageOutJson(Response response) throws IOException {
        String base64img;
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(response.body().string());
        base64img = rootNode.get("image").asText();

        return base64img;
    }

    private static void saveAsPng(byte[] imageBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("output.png")) {
            fos.write(imageBytes);
        }
    }

    //########################################## IMAGE SECTION #######################################################



    //########################################## UPDATE SECTION ######################################################

    //updates list of updates because method onUpdateReceived doesn't know which update was later or sooner
    //it doesn't have the history of updates. In order to get previous update, I need this method.
    //Max capacity is 2 so not to store more than 2 updates, when we receive an update, firstly we
    //move element(1) to index 0, than add an update to index 1
    private void updateReceived(Update update) {
        var chatId = update.getMessage().getChatId();
        var message = update.getMessage().getText();

        if (updateRepository.findById(chatId).isEmpty()) {
            UserUpdate userUpdate = new UserUpdate();
            userUpdate.setChatId(chatId);
            userUpdate.setUpdateBeforeLast(null);
            userUpdate.setLastUpdate(message);

            updateRepository.save(userUpdate);
        } else {
            UserUpdate userUpdate = updateRepository.findByChatId(chatId);
            userUpdate.setUpdateBeforeLast(userUpdate.getLastUpdate());
            userUpdate.setLastUpdate(message);

            updateRepository.save(userUpdate);
        }
    }

    private boolean checkIfUpdateContains(int position, Long chatId, String text) {
        return Objects.requireNonNull(getUpdateOnPosition(position, chatId)).contains(text);
    }

    private String getUpdateOnPosition(int position, Long chatId) {
        UserUpdate userUpdate = updateRepository.findByChatId(chatId);
        return switch (position) {
            case 1 -> userUpdate.getLastUpdate();
            case 0 -> userUpdate.getUpdateBeforeLast();
            default -> null;
        };
    }

    //########################################## UPDATE SECTION ######################################################



    //########################################## OTHER METHODS #######################################################

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
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

    //########################################## OTHER METHODS #######################################################
}