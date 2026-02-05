package com.lexer.demo.ui;

import javax.swing.*;
import java.awt.*;

public class GreenNeonTheme {

    public static final Color BG_DARK = new Color(0, 24, 18);
    public static final Color PANEL_BG = new Color(0, 40, 30);
    public static final Color TEXT_GREEN = new Color(0, 255, 150);
    public static final Color INPUT_BG = new Color(0, 32, 24);
    public static final Color CARET = new Color(0, 255, 120);

    public static final Font MONO =
            new Font("Monospaced", Font.PLAIN, 14);

    public static void apply() {
        UIManager.put("TextArea.background", PANEL_BG);
        UIManager.put("TextArea.foreground", TEXT_GREEN);
        UIManager.put("TextArea.caretForeground", CARET);
        UIManager.put("TextArea.font", MONO);

        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", TEXT_GREEN);
        UIManager.put("TextField.caretForeground", CARET);
        UIManager.put("TextField.font", MONO);

        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("Panel.background", BG_DARK);
    }
}
