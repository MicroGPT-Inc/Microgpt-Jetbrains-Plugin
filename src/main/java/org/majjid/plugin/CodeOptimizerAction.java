package org.majjid.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

// 1. Main Plugin Action Class
public class CodeOptimizerAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            Messages.showErrorDialog("No active project found.", "Error");
            return;
        }

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            Messages.showErrorDialog("No active editor found.", "Error");
            return;
        }

        Document document = editor.getDocument();
        String fileContent = document.getText();

        // Security: Redact sensitive information
        String sanitizedContent = SecurityUtil.redactSensitiveInfo(fileContent);

        // Fetch suggestions from AI
        String suggestions = CodeAnalysisService.getSuggestionsChatGPT(sanitizedContent);

        CodeSuggestionToolWindow.showSuggestions(project, suggestions, editor, document);
    }
}

// 2. Security Utility for Redacting Sensitive Information
class SecurityUtil {
    public static String redactSensitiveInfo(String content) {
        // Example: Replace API keys or sensitive patterns with placeholders
        return content.replaceAll("(?i)(api_key\s*=\s*['\"])[^'\"]+", "$1[REDACTED]");
    }
}

// Side panel Tool Window to show suggestions
class CodeSuggestionToolWindow {
    public static void showSuggestions(Project project, String suggestions, Editor editor, Document document) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Code Suggestions");

        if (toolWindow == null) {
            toolWindowManager.registerToolWindow("Code Suggestions", true, ToolWindowAnchor.RIGHT);
            toolWindow = toolWindowManager.getToolWindow("Code Suggestions");
        }

        if (!toolWindow.isAvailable()) {
            toolWindow.setAvailable(true);
        }

        ContentFactory contentFactory = ContentFactory.getInstance();
        JPanel panel = new JPanel(new BorderLayout());

        // Text area to display suggestions
        JTextArea suggestionArea = new JTextArea(suggestions);
        suggestionArea.setEditable(false);
        panel.add(new JScrollPane(suggestionArea), BorderLayout.CENTER);

        // Text box for user query input
        JPanel queryPanel = new JPanel(new BorderLayout());
        JLabel queryLabel = new JLabel("Enter your query:");
        JTextField queryField = new JTextField();
        queryPanel.add(queryLabel, BorderLayout.WEST);
        queryPanel.add(queryField, BorderLayout.CENTER);
        panel.add(queryPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));


        // Apply button
        JButton applyButton = new JButton("Apply Changes");
        applyButton.addActionListener(e -> {
            String updatedSuggestions = suggestionArea.getText();
            ApplicationManager.getApplication().runWriteAction(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    document.setText(updatedSuggestions);
                });
            });
            Messages.showInfoMessage("Suggestions applied successfully!", "Success");
//            toolWindow.hide(null); // Hide the tool window after applying changes
        });

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        ToolWindow finalToolWindow = toolWindow;
        cancelButton.addActionListener(e -> {
            Messages.showInfoMessage("Suggestions were not applied.", "Cancelled");
            finalToolWindow.hide(null); // Hide the tool window
        });

        // Submit Query button for chatgpt
        JButton submitQueryButtonChatgpt = new JButton("ChatGPT");
        submitQueryButtonChatgpt.addActionListener(e -> {
            String userQuery = queryField.getText().trim();
            if (userQuery.isEmpty()) {
                Messages.showWarningDialog("Query cannot be empty!", "Invalid Query");
            } else {
                // Placeholder: Call your AI service or query processor with user input
                String fileContent = document.getText();
                // Security: Redact sensitive information
                String sanitizedContent = SecurityUtil.redactSensitiveInfo(fileContent);
                String newSuggestions = CodeAnalysisService.getSuggestionsChatGPT(userQuery + " " + sanitizedContent);
                suggestionArea.setText(newSuggestions);
            }
        });


        // Submit Query button for chatgpt
        JButton submitQueryButtonGemini = new JButton("Gemini");
        submitQueryButtonGemini.addActionListener(e -> {
            String userQuery = queryField.getText().trim();
            if (userQuery.isEmpty()) {
                Messages.showWarningDialog("Query cannot be empty (Gemini)!", "Invalid Query");
            } else {
                // Placeholder: Call your AI service or query processor with user input
                String fileContent = document.getText();
                // Security: Redact sensitive information
                String sanitizedContent = SecurityUtil.redactSensitiveInfo(fileContent);
                String newSuggestions = CodeAnalysisService.getSuggestionsGemini(userQuery + " " + sanitizedContent);
                suggestionArea.setText(newSuggestions);
            }
        });

        // Add buttons to the button panel
        buttonPanel.add(submitQueryButtonGemini);
        buttonPanel.add(submitQueryButtonChatgpt);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        // Add button panel to the main panel
        panel.add(buttonPanel, BorderLayout.SOUTH);

        Content content = contentFactory.createContent(panel, "AI Suggestions", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);

        toolWindow.show(null);
    }
}

// 5. Suggestion Preview UI
class SuggestionPreview {
    public static boolean showSuggestions(Project project, String suggestions) {
        int result = Messages.showYesNoDialog(
                project,
                "Suggested Changes:\n\n" + suggestions,
                "Code Optimizer Suggestions",
                "Apply Changes",
                "Cancel",
                Messages.getQuestionIcon()
        );
        return result == Messages.YES;
    }
}
