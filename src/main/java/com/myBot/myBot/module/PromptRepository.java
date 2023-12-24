package com.myBot.myBot.module;

import org.springframework.data.repository.CrudRepository;

public interface PromptRepository extends CrudRepository<Prompt, Long> {
    Prompt findByChatId (Long chatId);
}
