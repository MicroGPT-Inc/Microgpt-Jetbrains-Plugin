package org.majjid.plugin.geminiutils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;

import java.io.FileInputStream;
import java.io.IOException;

public class GeminiChatIntegration {

    private static final String projectId = "axial-device-436818-b4";
    private static final String location = "us-east1";
    private static final String modelName = "gemini-pro";

    public static String fetchCodeSuggestionsFromGemini(String code) {

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            ChatSession chatSession = new ChatSession(model);

            GenerateContentResponse response;
            response = chatSession.sendMessage(code);
            System.out.println("gemini : " + response);
            return "gemini" + ResponseHandler.getText(response);

        } catch (Exception e) {
            System.out.println("Error communicating with Gemini: " + e.getMessage());
        }
        return code;
    }
}

