package com.lexer.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SwingShell extends JFrame {

    private final JTextArea textArea = new JTextArea();
    private final Executor executor;

    // Where user input begins (after "$ ")
    private int promptPosition = 0;

    public SwingShell(Executor executor) {
        this.executor = executor;

        setTitle("ElfHelper");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea.setFont(new Font("Menlo", Font.PLAIN, 14));
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setCaretColor(Color.GREEN);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        installKeyHandling();
        showPrompt();

        setVisible(true);
    }

    // ---------------- PROMPT ----------------
    private void showPrompt() {
        String prompt = buildPrompt();
        textArea.append(prompt);
        promptPosition = textArea.getDocument().getLength();
        textArea.setCaretPosition(promptPosition);
    }

    private String buildPrompt() {
        File dir = executor.getCurrentDirectory();
        String home = System.getProperty("user.home");
        String path = dir.getAbsolutePath().replace(home, "~");
        return path + " $ ";
    }

    // ---------------- KEY HANDLING ----------------
    private void installKeyHandling() {
        textArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int caret = textArea.getCaretPosition();

                // Block backspace before prompt
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && caret <= promptPosition) {
                    e.consume();
                }

                // Block left arrow before prompt
                if (e.getKeyCode() == KeyEvent.VK_LEFT && caret <= promptPosition) {
                    e.consume();
                }

                // Disable up/down arrows for now (future: history)
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.consume();
                }

                // ENTER executes
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    executeCommand();
                }
            }
        });
    }

    // ---------------- EXECUTION ----------------
    private void executeCommand() {
        try {
            String fullText = textArea.getText();
            String command = fullText.substring(promptPosition).trim();

            textArea.append("\n");

            if (!command.isEmpty()) {

                // ✅ Correct Lexer → Parser → Executor flow
                Lexer lexer = new Lexer(command);
                List<Token> tokens = lexer.tokenize();
                Parser parser = new Parser(tokens);
                PipelineNode pipeline = parser.parse();

                String output = executor.executeAndCapture(pipeline);

                if ("__CLEAR__".equals(output)) {
                    textArea.setText("");
                } else {
                    textArea.append(output);
                }
            }

        } catch (Exception e) {
            textArea.append("error\n");
        }

        showPrompt();
    }
}
