package com.lexer.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SwingShell extends JFrame {

    private final JTextArea textArea = new JTextArea();
    private final Executor executor;
    private int promptPosition = 0;

    private Point initialClick;
    private boolean maximized = false;

    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;

    private final String[] commands = {"ls", "cd", "pwd", "clear", "rm", "echo"};

    public SwingShell(Executor executor) {
        this.executor = executor;

        setUndecorated(true);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);
        add(createTerminalPane(), BorderLayout.CENTER);

        installKeyHandling();
        showPrompt();

        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(18, 18, 18));
        topBar.setPreferredSize(new Dimension(100, 32));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttons.setOpaque(false);
        buttons.add(createControlButton(Color.RED, () -> System.exit(0)));
        buttons.add(createControlButton(Color.ORANGE, () -> setState(Frame.ICONIFIED)));
        buttons.add(createControlButton(Color.GREEN, this::toggleMaximize));

        JLabel title = new JLabel("RocketUI Terminal", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(loadOrbitronFont(16f));

        topBar.add(buttons, BorderLayout.WEST);
        topBar.add(title, BorderLayout.CENTER);

        topBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        topBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        return topBar;
    }

    private JPanel createControlButton(Color color, Runnable action) {
        JPanel c = new JPanel();
        c.setBackground(color);
        c.setPreferredSize(new Dimension(12, 12));
        c.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        c.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { action.run(); }
        });
        return c;
    }

    private void toggleMaximize() {
        if (!maximized) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            maximized = true;
        } else {
            setExtendedState(JFrame.NORMAL);
            maximized = false;
        }
    }

    private JScrollPane createTerminalPane() {
        textArea.setFont(loadOrbitronFont(18f));
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setCaretColor(Color.GREEN);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea.setFocusTraversalKeysEnabled(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private void installKeyHandling() {
        textArea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "tabAction");
        textArea.getActionMap().put("tabAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { autocomplete(); }
        });

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int caret = textArea.getCaretPosition();

                if (caret < promptPosition) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_DOWN:
                            handleHistory(e);
                            return;
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && caret <= promptPosition) e.consume();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    executeCommand();
                }

                handleHistory(e); // allow up/down for history navigation
            }
        });
    }

    private void autocomplete() {
        String text = textArea.getText().substring(promptPosition).trim();
        for (String cmd : commands) {
            if (cmd.startsWith(text)) {
                replacePromptWith(cmd);
                break;
            }
        }
    }

    private void handleHistory(KeyEvent e) {
        if (history.isEmpty()) return;

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (historyIndex > 0) historyIndex--;
            else if (historyIndex == -1) historyIndex = history.size() - 1;
            replacePromptWith(history.get(historyIndex));
            e.consume();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (historyIndex < history.size() - 1) historyIndex++;
            else {
                historyIndex = history.size();
                replacePromptWith("");
                e.consume();
                return;
            }
            replacePromptWith(history.get(historyIndex));
            e.consume();
        }
    }

    private void replacePromptWith(String cmd) {
        try {
            textArea.getDocument().remove(promptPosition, textArea.getDocument().getLength() - promptPosition);
            textArea.getDocument().insertString(promptPosition, cmd, null);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } catch (Exception ignored) {}
    }

    private void showPrompt() {
        textArea.append(buildPrompt());
        promptPosition = textArea.getDocument().getLength();
        textArea.setCaretPosition(promptPosition);
    }

    private String buildPrompt() {
        File dir = executor.getCurrentDirectory();
        String home = System.getProperty("user.home");
        String path = dir.getAbsolutePath().replace(home, "~");
        return path + " $ ";
    }

    private void executeCommand() {
        String fullText = textArea.getText();
        String command = fullText.substring(promptPosition).trim();
        textArea.append("\n");

        if (!command.isEmpty()) {
            history.add(command);
            historyIndex = history.size();

            String output = executor.executeAndCapture(Parser.parseSingleCommand(command));
            if ("__CLEAR__".equals(output)) textArea.setText("");
            else textArea.append(output);
        }

        showPrompt();
    }

    private Font loadOrbitronFont(float size) {
        try {
            InputStream is = getClass().getResourceAsStream("/static/fonts/Orbitron-VariableFont_wght.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(Font.PLAIN, size);
        } catch (Exception e) {
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
    }
}