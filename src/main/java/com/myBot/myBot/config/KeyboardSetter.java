package com.myBot.myBot.config;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardSetter {


    public static ReplyKeyboardMarkup startMenu () {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

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
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("of tea");
        row.add("of coffee");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }



}
