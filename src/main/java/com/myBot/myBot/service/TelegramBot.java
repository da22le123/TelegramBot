package com.myBot.myBot.service;
import com.myBot.myBot.config.BotConfig;
import com.myBot.myBot.config.KeyboardSetter;
import com.myBot.myBot.module.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.*;


@Slf4j
@Component
public final class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UpdateRepository updateRepository;
    @Autowired
    private PromptRepository promptRepository;
    @Autowired
    private PhotoSaverService photoSaverService;
    final BotConfig config;

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
                    "2) /help - get information about bot`s usage" + "\n" +
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
            log.error("Error setting bot`s command list: " + e.getMessage());
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
                case "/photo" -> {
                    sendMessage(chatId, "Pick a prompt, let's start with the style of picture. All of the options are displayed in the Screen Keyboard.",
                        KeyboardSetter.styleMenu());
                    resetPrompt(chatId);
                }



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

    private void checkPromptAndGenerate(Long chatId){


        if (checkAndSetStyle(chatId)) {
            if (AnimeStyle(chatId)) {
                sendMessage(chatId, "Pick the view you want.", KeyboardSetter.viewMenu());
            } else {
                sendMessage(chatId, "Okay, now let's pick an age of a girl you'd like to generate.", KeyboardSetter.ageMenu());
            }
        }

        if (AnimeStyle(chatId)){
            if (checkAndSetViewPrompt(chatId)) {
                if (frontOrSideView(chatId)) {
                    sendMessage(chatId, "Let's choose the breast size then.", KeyboardSetter.boobsMenu());
                } else {
//                    String lastPrompt = prompt;
//                    String newPrompt = ;
//                    prompt = lastPrompt + newPrompt;
                    promptUpdate(chatId, " big ass, best ass, detailed asshole, detailed vagina ");
                    sendFinalPhoto(chatId);
                }
            }
        } else {
            if (checkAndSetAgePrompt(chatId)) {
                sendMessage(chatId, "Pick the view you want.", KeyboardSetter.viewMenu());
            }
        }

        if (AnimeStyle(chatId)) {
            if (frontOrSideView(chatId)) {
                if (checkAndSetBreastPromptAfterView(chatId)) {
                    sendMessage(chatId, "What color should the hair be?", KeyboardSetter.hairMenu());
                }
            }
        } else {
            if (checkAndSetViewPromptAfterAge(chatId)) {
                if (frontOrSideView(chatId)) {
                    sendMessage(chatId, "Pick the nationality of the girl", KeyboardSetter.nationalityMenu());
                } else {
                    promptUpdate(chatId, " big ass, best ass, detailed asshole, detailed vagina ");
                    sendMessage(chatId, "Pick the type of body you want", KeyboardSetter.bodyMenu());
                }
            }
        }

        if (AnimeStyle(chatId)) {
            if (frontOrSideView(chatId)) {
                if (checkAndSetHairPromptAfterBreast(chatId)) {
                    sendFinalPhoto(chatId);
                }
            }
        } else {
            if (frontOrSideView(chatId)) {
                if (checkAndSetNationalityPromptAfterView(chatId)) {
                    sendMessage(chatId, "Pick the type of body you want", KeyboardSetter.bodyMenu());
                }
            } else {
                if (checkAndSetBodyPromptAfterView(chatId)){
                    sendMessage(chatId, "What color should the hair be?", KeyboardSetter.hairMenu());
                }
            }
        }

        if (!AnimeStyle(chatId)) {
            if (frontOrSideView(chatId)) {
                if (checkAndSetBodyPromptAfterNationality(chatId)) {
                    sendMessage(chatId, "Let's choose the breast size then.", KeyboardSetter.boobsMenu());
                }
            } else {
                if (checkAndSetHairPromptAfterBody(chatId)) {
                    sendFinalPhoto(chatId);
                }
            }
        }

        if (!AnimeStyle(chatId)) {
            if (frontOrSideView(chatId)) {
                if (checkAndSetBreastPrompt(chatId)) {
                    sendMessage(chatId, "What color should the hair be?", KeyboardSetter.hairMenu());
                }
            }
        }

        if (!AnimeStyle(chatId)) {
            if (frontOrSideView(chatId)) {
                if (checkAndSetHairPromptAfterBreast(chatId)) {
                    sendFinalPhoto(chatId);
                }
            }
        }
    }

    private void sendFinalPhoto(Long chatId) {
        try {
            try {
                sendMessage(chatId, "Here is your image:", KeyboardSetter.startMenu());
                execute(photoSaverService.sendImage(chatId,
                        promptRepository.findByChatId(chatId).getView() + " , " + promptRepository.findByChatId(chatId).getPrompt(),
                        promptRepository.findByChatId(chatId).getModelId()));
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private boolean checkAndSetStyle(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "/photo")) {
            if (checkIfUpdateContains(1, chatId, "Anime style")) {
                //modelId = "dark-sushi-mix-v2-25";
                modelIdUpdate(chatId, "dark-sushi-mix-v2-25");

                return true;
            } else if (checkIfUpdateContains(1, chatId, "Realistic style")) {
                //modelId = "realistic-vision-v5-1";
                modelIdUpdate(chatId, "realistic-vision-v5-1");
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
                //prompt = getUpdateOnPosition(1, chatId);
                promptUpdate(chatId, getUpdateOnPosition(1, chatId));
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetNationalityPromptAfterView(Long chatId){
        if (checkIfUpdateContains(0, chatId, "view")) {
            if (checkIfUpdateContains(1, chatId, "an")
                    || checkIfUpdateContains(1, chatId, "Latina")
                    || checkIfUpdateContains(1, chatId, "Black")
                    || checkIfUpdateContains(1, chatId, "Slavic")) {
                promptUpdate(chatId, getUpdateOnPosition(1, chatId) + " appearance");
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetViewPrompt(Long chatId){
        if (checkIfUpdateContains(0, chatId, "style")) {
            if (checkIfUpdateContains(1, chatId, "view")) {
                viewUpdate(chatId, getUpdateOnPosition(1, chatId) + " ");
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetViewPromptAfterAge(Long chatId){
        if (checkIfUpdateContains(0, chatId, "yo")) {
            if (checkIfUpdateContains(1, chatId, "view")) {
                viewUpdate(chatId, getUpdateOnPosition(1, chatId) + " ");
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetBodyPromptAfterNationality(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "an")
                || checkIfUpdateContains(0, chatId, "Latina")
                || checkIfUpdateContains(0, chatId, "Black")
                || checkIfUpdateContains(0, chatId, "Slavic")) {
            if (checkIfUpdateContains(1, chatId, "body")) {
                promptUpdate(chatId, "fully naked girl, real, masterpiece, best quality, body parts detailed," +
                      "(detail skin texture, ultra-detailed body)" + getUpdateOnPosition(1, chatId) + " ");
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetBodyPromptAfterView(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "view")) {
            if (checkIfUpdateContains(1, chatId, "body")) {
                promptUpdate(chatId, "fully naked girl, real, masterpiece, best quality, body parts detailed," +
                        "(detail skin texture, ultra-detailed body)" + getUpdateOnPosition(1, chatId) + " ");
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
              promptUpdate(chatId, getUpdateOnPosition(1, chatId) + " ");
            return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetBreastPromptAfterView(Long chatId) {
        if (checkIfUpdateContains(0, chatId, "view")) {
            if (checkIfUpdateContains(1, chatId, "breast")) {
                promptUpdate(chatId, "fully naked girl, real, masterpiece, best quality, body parts detailed," +
                        "(detail skin texture, ultra-detailed body)" + getUpdateOnPosition(1, chatId) + " ");
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetHairPromptAfterBody(Long chatId){
        if (checkIfUpdateContains(0, chatId, "body")) {
            if (checkIfUpdateContains(1, chatId, "hair")) {
                promptUpdate(chatId, getUpdateOnPosition(1, chatId) + " ");
                return true;
            } else {
                startAllOver(chatId);
            }
        }
        return false;
    }

    private boolean checkAndSetHairPromptAfterBreast(Long chatId){
        if (checkIfUpdateContains(0, chatId, "breast")) {
            if (checkIfUpdateContains(1, chatId, "hair")) {
                promptUpdate(chatId, getUpdateOnPosition(1, chatId) + " ");
                return true;
            }
        }
        return false;
    }

    private void promptUpdate(Long chatId, String promptText) {
        if (promptRepository.findById(chatId).isEmpty()){
            Prompt prompt = new Prompt();
            prompt.setChatId(chatId);
            prompt.setPrompt(promptText);
            
            promptRepository.save(prompt);
        } else {
            Prompt prompt = promptRepository.findByChatId(chatId);
            var lastPrompt = prompt.getPrompt();
            prompt.setPrompt(lastPrompt + " , " + promptText);
            
            promptRepository.save(prompt);
        }
    }
    
    private void modelIdUpdate(Long chatId, String modelId) {
        if (promptRepository.findById(chatId).isEmpty()) {
            Prompt prompt = new Prompt();
            prompt.setChatId(chatId);
            prompt.setModelId(modelId);

            promptRepository.save(prompt);
        } else {
            Prompt prompt = promptRepository.findByChatId(chatId);
            prompt.setChatId(chatId);
            prompt.setModelId(modelId);

            promptRepository.save(prompt);
        }
    }

    private void viewUpdate(Long chatId, String view){
        if (promptRepository.findById(chatId).isEmpty()) {
            Prompt prompt = new Prompt();
            prompt.setChatId(chatId);
            prompt.setView(view);

            promptRepository.save(prompt);
        } else {
            Prompt prompt = promptRepository.findByChatId(chatId);
            prompt.setChatId(chatId);
            prompt.setView(view);

            promptRepository.save(prompt);
        }
    }

    private void resetPrompt(Long chatId) {
        Prompt prompt = promptRepository.findByChatId(chatId);
        if (prompt != null) {
            prompt.resetAll(chatId);
            promptRepository.save(prompt);
        }
    }

    private void startAllOver(Long chatId) {
        sendMessage(chatId, "You should follow along and only pick one of the suggested prompts, try " +
                "generating one more time starting from the command /photo.", KeyboardSetter.startMenu());
    }

    private boolean AnimeStyle(Long chatId){
        return promptRepository.findByChatId(chatId)!=null && promptRepository.findByChatId(chatId).getModelId()!=null && promptRepository.findByChatId(chatId).getModelId().equals("dark-sushi-mix-v2-25");
    }

    private boolean frontOrSideView(Long chatId) {
        return promptRepository.findByChatId(chatId)!=null && promptRepository.findByChatId(chatId).getView()!=null &&
              (promptRepository.findByChatId(chatId).getView().contains("Front") ||
               promptRepository.findByChatId(chatId).getView().contains("Side"));
    }



    //########################################## PROMPT SECTION ######################################################

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
        return getUpdateOnPosition(position, chatId) != null && getUpdateOnPosition(position, chatId).contains(text);
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