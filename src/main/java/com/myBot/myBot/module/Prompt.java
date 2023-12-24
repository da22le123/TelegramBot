package com.myBot.myBot.module;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "userPromptTable")
public class Prompt {
    @Id
    private Long chatId;
    private String prompt;
    private String modelId;
    private String view;


    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void resetAll(Long chatId) {
        this.chatId = chatId;
        this.prompt = "";
        this.modelId = "";
        this.view = "";
    }
}
