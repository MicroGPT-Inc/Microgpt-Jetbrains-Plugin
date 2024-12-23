package org.majjid.plugin;

import com.intellij.openapi.components.Service;

import static org.majjid.plugin.geminiutils.GeminiChatIntegration.fetchCodeSuggestionsFromGemini;
import static org.majjid.plugin.openaiutils.OpenAiApi.fetchCodeSuggestionsFromChatGPT;

@Service
public final class CodeAnalysisService {

    public static String getSuggestionsChatGPT(String code) {
        // Example OpenAI/Gemini integration (replace with actual API calls)
        try {
            return fetchCodeSuggestionsFromChatGPT(code);
        } catch (Exception e) {
            return "Error fetching suggestions: " + e.getMessage();
        }
    }

    public static String getSuggestionsGemini(String code) {
        // Example OpenAI/Gemini integration (replace with actual API calls)
        try {
            return fetchCodeSuggestionsFromGemini(code);
        } catch (Exception e) {
            return "Error fetching suggestions: " + e.getMessage();
        }
    }
}
