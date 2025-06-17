package com.wldst.ruder.module.ai.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class OllamaIntegration {
    private static final String OLLAMA_ENDPOINT = "http://localhost:9444/api/generate";

    public String generateResponse(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient();
        GenerateRequest llama2 = new GenerateRequest(prompt, "llama2");

        Request request = new Request.Builder()
                .url(OLLAMA_ENDPOINT)
                .post(RequestBody.create(new Gson().toJson(llama2, GenerateRequest.class.getClass()).getBytes(StandardCharsets.UTF_8)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code: " + response);
            return response.body().string();
        }
    }
}

class GenerateRequest {
    public String prompt;
    public String model;

    public GenerateRequest(String prompt, String model) {
        this.prompt = prompt;
        this.model = model;
    }
}

