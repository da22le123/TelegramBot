package com.myBot.myBot.config;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardSetter {
    static ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    private static List<KeyboardRow> keyboardRows = new ArrayList<>();


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

    public static ReplyKeyboardMarkup promptMenu () {
        keyboardRows.clear();

        KeyboardRow row = new KeyboardRow();
        row.add("of tea");
        row.add("of coffee");
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
        row.add("Chubby body");
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
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Red hair");
        row.add("Brown hair");
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


}
