package com.myBot.myBot.config;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardSetter {
    static ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private static final List<KeyboardRow> keyboardRows = new ArrayList<>();


    public static ReplyKeyboardMarkup startMenu () {
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("/help");
        row.add("/settings");
        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("/my_data");
        row.add("/photo");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup styleMenu() {
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("Anime style");
        row.add("Realistic style");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup ageMenu() {
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("18 yo");
        row.add("20 yo");
        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("30 yo");
        row.add("40 yo");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup bodyMenu(){
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("Slim body");
        row.add("Chubby / Fat body");
        row.add("Athletic body");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup boobsMenu() {
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("Small breast");
        row.add("Common-size breast");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Big breast");
        row.add("Gigantic breast");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup hairMenu(){
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("Blonde hair");
        row.add("Black hair");
        row.add("Pink hair");
        row.add("Blue hair");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Red hair");
        row.add("Brown hair");
        row.add("Green hair");
        row.add("Gray hair");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup placeMenu() {
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("On a beach");
        row.add("On a bed");
        row.add("On a chair");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("On a couch");
        row.add("On a bike");
        row.add("In a car");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup nationalityMenu(){
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("Asian");
        row.add("European");
        row.add("Scandinavian");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Latina");
        row.add("Black");
        row.add("Slavic");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup viewMenu(){
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("Front view");
        row.add("Front close up view");
        row.add("Side view / Profile view");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Ass view");
        row.add("Ass close up view");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }


}
