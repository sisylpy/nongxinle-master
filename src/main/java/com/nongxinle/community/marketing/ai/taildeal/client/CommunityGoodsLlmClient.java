package com.nongxinle.community.marketing.ai.taildeal.client;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Component
public class CommunityGoodsLlmClient {

    private static final Logger log = LoggerFactory.getLogger(CommunityGoodsLlmClient.class);

    @Value("${deepseek.api.key:}")
    private String deepSeekApiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepSeekApiUrl;

    public JSONObject extractTailDealContract(String rawText, String defaultMarketCloseTime) throws IOException {
        String systemPrompt = loadPrompt();
        String userContent = "rawText:\n" + rawText;
        if (defaultMarketCloseTime != null && !defaultMarketCloseTime.isEmpty()) {
            userContent += "\ndefaultMarketCloseTime: " + defaultMarketCloseTime;
        }
        String content = callChat(systemPrompt, userContent);
        JSONObject contract = parseJsonContent(content);
        log.info("[TailDealAI] llmExtract ok rawLen={}, contract={}", rawText.length(), contract);
        return contract;
    }

    private String loadPrompt() throws IOException {
        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("ai-prompts/taildeal/taildeal_adsense_extract.v1.md");
        if (in == null) {
            throw new IOException("prompt file not found");
        }
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private String callChat(String systemPrompt, String userContent) throws IOException {
        if (deepSeekApiKey == null || deepSeekApiKey.trim().isEmpty()) {
            throw new IOException("deepseek.api.key not configured");
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(120000)
                .setConnectionRequestTimeout(30000)
                .build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(deepSeekApiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);
        httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8.name()));

        long startMs = System.currentTimeMillis();
        CloseableHttpResponse response = client.execute(httpPost);
        try {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
            JSONObject jsonResponse = JSONObject.fromObject(responseBody);
            if (jsonResponse.containsKey("error")) {
                String err = jsonResponse.getJSONObject("error").optString("message", "LLM error");
                log.error("[TailDealAI] llmCallError msg={}", err);
                throw new IOException(err);
            }
            JSONArray choices = jsonResponse.getJSONArray("choices");
            String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
            log.info("[TailDealAI] llmCall ok costMs={}, contentLen={}",
                    System.currentTimeMillis() - startMs, content.length());
            return content;
        } finally {
            response.close();
            client.close();
        }
    }

    private JSONObject parseJsonContent(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }
        return JSONObject.fromObject(trimmed);
    }
}
