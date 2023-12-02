package com.myBot.myBot.module;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UpdateRepository extends CrudRepository<UserUpdate, Long>{
    UserUpdate findLastUpdateByChatId(Long chatId);
    UserUpdate findUpdateBeforeLastByChatId(Long chatId);
    UserUpdate findByChatId (Long chatId);
}
