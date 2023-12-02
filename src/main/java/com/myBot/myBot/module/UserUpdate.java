package com.myBot.myBot.module;

import jakarta.persistence.*;

@Entity(name = "userUpdatesTable")
public class UserUpdate {

    @Id
    private Long chatId;
    private String lastUpdate;
    private String updateBeforeLast;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getUpdateBeforeLast() {
        return updateBeforeLast;
    }

    public void setUpdateBeforeLast(String updateBeforeLast) {
        this.updateBeforeLast = updateBeforeLast;
    }
}
