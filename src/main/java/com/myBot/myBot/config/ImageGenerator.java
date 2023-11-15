package com.myBot.myBot.config;
import lombok.Data;
import okhttp3.*;

import java.io.IOException;
@Data
public class ImageGenerator {
     String prompt;


    public Response sendRequest() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"prompt\":\"" + getPrompt() + "\",\"negative_prompt\":\"blurred\"}");
        Request request = new Request.Builder()
                .url("https://api.getimg.ai/v1/stable-diffusion/text-to-image")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer key-4OnNEotW7oAL3uIq6AnEl0gR7IgCJSLHoZozuZgeA2MauEXFQDJHlyjjO8rT5LYLJ2A0fcvMUCoVYNmnvjWKi27TxEiyuW1G")
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }



}
