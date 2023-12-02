package com.myBot.myBot.config;
import lombok.Data;
import okhttp3.*;

import java.io.IOException;
@Data
public class ImageGenerator {
     String prompt;
     String modelId;


    public Response sendRequest() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"model\":\"" + getModelId() + "\",\"prompt\":\"" + getPrompt() + "\",\"negative_prompt\":\"worst quality, low quality, (deformed, distorted, disfigured:1), poorly drawn, bad anatomy, wrong anatomy, extra limb, missing limb, floating limbs, (mutated hands and fingers:1.4), disconnected limbs, mutation, mutated, (ugly), disgusting, blurry, amputation, cloned\"}");
        Request request = new Request.Builder()
                .url("https://api.getimg.ai/v1/stable-diffusion/text-to-image")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer key-1D1rt6hCZTodvo2kVOWCMEFR0KVUChNspNOTHuFRVjz7q32AZURVER7uGFGSvxbTDyK9R8c4N9BC1enkEy6wdOOAZwLcDLPD")
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
