package org.majjid.plugin.geminiutils;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.intellij.openapi.diagnostic.Logger;

public class GeminiChatIntegration {

    private static final String PROJECT_ID = "axial-device-436818-b4";
    private static final String LOCATION = "us-east1";
    private static final String MODEL_NAME = "gemini-pro";
    private static final Logger logger = Logger.getInstance(GeminiChatIntegration.class);

    private GeminiChatIntegration() {
    }

    public static String fetchCodeSuggestionsFromGemini(String code) {

        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);
            ChatSession chatSession = new ChatSession(model);

            GenerateContentResponse response;
            response = chatSession.sendMessage(code);
//            logger.info("gemini : {}", response);
            return ResponseHandler.getText(response);

        } catch (Exception e) {
            logger.error("Error communicating with Gemini: {}", e.getMessage());
        }
        return code;
    }
}