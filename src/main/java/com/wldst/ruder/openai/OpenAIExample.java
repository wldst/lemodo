package com.wldst.ruder.openai;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
public class OpenAIExample {
    public static void main(String[] args) throws Exception {
        // Set API endpoint URL
        String url = "https://api.openai.com/v1/engines/davinci-codex/completions";
        // Set API key
        String apiKey = "FC3U1YLDCZZNACYDM8";
        // Set request parameters
        String prompt = "Hello, my name is";
        int maxTokens = 5;
        boolean stop = true;
        // Create JSON request body
        String jsonBody = String.format("{\"prompt\": \"%s\", \"max_tokens\": %d, \"stop\": %s}", prompt, maxTokens, stop);
        // Send HTTP POST request to OpenAI API
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + apiKey);
        StringEntity body = new StringEntity(jsonBody);
        request.setEntity(body);
        HttpResponse response = client.execute(request);
        // Parse response data
        HttpEntity entity = response.getEntity();
        String responseData = EntityUtils.toString(entity);
        System.out.println(responseData);
    }
}
