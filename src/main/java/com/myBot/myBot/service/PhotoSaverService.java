package com.myBot.myBot.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myBot.myBot.config.ImageGenerator;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PhotoSaverService {
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

    public SendPhoto sendImage(long chatId, String prompt, String modelId) throws IOException {
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
            return photo;
        } catch (IOException e) {
            log.error("Error occurred: " + e.getMessage());
            return null;
        }
    }

}
