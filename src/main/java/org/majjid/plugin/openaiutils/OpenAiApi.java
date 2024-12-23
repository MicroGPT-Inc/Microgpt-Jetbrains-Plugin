package org.majjid.plugin.openaiutils;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;

public class OpenAiApi {
    public static String fetchCodeSuggestionsFromChatGPT(String code) throws Exception {
        // Example API integration (pseudo-code for simplicity)
        String apiKey = "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";

        String requestBody = "{\n" +
                "  \"prompt\": \"Analyze and optimize the following code:\n" + code +
                "\",\n You must return the code only. Add Comment in the code." +
                "}";

        // Make HTTP request
        String response = getResponse(requestBody, apiKey);
        System.out.println("chatgpt : " + response);
        return parseSuggestionsFromResponse(response);
    }

    public static String getResponse(String body, String apiKey) throws Exception {
        // Simplified HTTP POST request (use a proper HTTP library in production)
        OpenAiService service = new OpenAiService(apiKey);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), body);
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .build();
        ChatCompletionResult result = service.createChatCompletion(chatCompletionRequest);
        long usedTokens = result.getUsage().getTotalTokens();
        ChatMessage response = result.getChoices().get(0).getMessage();
        return response.getContent();
    }

    public static String parseSuggestionsFromResponse(String response) {
        // Parse and return suggestions
        return response; // Simplified for brevity
    }
}
