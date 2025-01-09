package org.majjid.plugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static final String CODE_SUGGESTIONS = "Code Suggestions";
    public static final String ENTER_YOUR_QUERY = "Enter your query:";
    public static final String SUGGESTIONS_APPLIED_SUCCESSFULLY = "Suggestions applied successfully!";
    public static final String SUCCESS = "Success";
    public static final String CANCEL = "Cancel";
    public static final String SUGGESTIONS_WERE_NOT_APPLIED = "Suggestions were not applied.";
    public static final String CANCELLED = "Cancelled";
    public static final String CHAT_GPT = "ChatGPT";
    public static final String QUERY_CANNOT_BE_EMPTY = "Query cannot be empty!";
    public static final String INVALID_QUERY = "Invalid Query";
    public static final String GEMINI = "Gemini";
    public static final String QUERY_CANNOT_BE_EMPTY_GEMINI = "Query cannot be empty (Gemini)!";
    public static final String AI_SUGGESTIONS = "AI Suggestions";
    public static final @Nullable Runnable NULL = null;
    public static final String TEXT = "Apply Changes";

    public static void showSuggestions(Project project, String suggestions, Editor editor, Document document) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(CODE_SUGGESTIONS);

        if (toolWindow == null) {
            toolWindowManager.registerToolWindow(RegisterToolWindowTask.closable(
                    CODE_SUGGESTIONS, // ID
                    AllIcons.General.Information,
                    ToolWindowAnchor.RIGHT // Anchor position
            ));
            toolWindow = toolWindowManager.getToolWindow(CODE_SUGGESTIONS);
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
        JLabel queryLabel = new JLabel(ENTER_YOUR_QUERY);
        JTextField queryField = new JTextField();
        queryPanel.add(queryLabel, BorderLayout.WEST);
        queryPanel.add(queryField, BorderLayout.CENTER);
        panel.add(queryPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));


        // Apply button
        JButton applyButton = new JButton(TEXT);
        applyButton.addActionListener(e -> {
            String updatedSuggestions = suggestionArea.getText();
            ApplicationManager.getApplication().runWriteAction(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    document.setText(updatedSuggestions);
                });
            });
            Messages.showInfoMessage(SUGGESTIONS_APPLIED_SUCCESSFULLY, SUCCESS);
//            toolWindow.hide(null); // Hide the tool window after applying changes
        });

        // Cancel button
        JButton cancelButton = new JButton(CANCEL);
        ToolWindow finalToolWindow = toolWindow;
        cancelButton.addActionListener(e -> {
            Messages.showInfoMessage(SUGGESTIONS_WERE_NOT_APPLIED, CANCELLED);
            finalToolWindow.hide(NULL); // Hide the tool window
        });

        // Submit Query button for chatgpt
        JButton submitQueryButtonChatgpt = new JButton(CHAT_GPT);
        submitQueryButtonChatgpt.addActionListener(e -> {
            String userQuery = queryField.getText().trim();
            if (userQuery.isEmpty()) {
                Messages.showWarningDialog(QUERY_CANNOT_BE_EMPTY, INVALID_QUERY);
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
        JButton submitQueryButtonGemini = new JButton(GEMINI);
        submitQueryButtonGemini.addActionListener(e -> {
            String userQuery = queryField.getText().trim();
            if (userQuery.isEmpty()) {
                Messages.showWarningDialog(QUERY_CANNOT_BE_EMPTY_GEMINI, INVALID_QUERY);
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

        Content content = contentFactory.createContent(panel, AI_SUGGESTIONS, false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);

        toolWindow.show(null);
    }
}

// 5. Suggestion Preview UI
class SuggestionPreview {

    public static final String CANCEL = "Cancel";
    public static final String YES_TEXT = "Apply Changes";
    public static final String TITLE = "Code Optimizer Suggestions";
    public static final String SUGGESTED_CHANGES = "Suggested Changes:\n\n";

    public static boolean showSuggestions(Project project, String suggestions) {
        int result = Messages.showYesNoDialog(
                project,
                SUGGESTED_CHANGES + suggestions,
                TITLE,
                YES_TEXT,
                CANCEL,
                Messages.getQuestionIcon()
        );
        return result == Messages.YES;
    }
}
