package com.lexer.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lexer.demo.SwingShell;

import org.springframework.boot.CommandLineRunner;
import javax.swing.SwingUtilities;

@Configuration
public class SwingLauncher {
    @Bean
    CommandLineRunner launchUI(SwingShell shell) {
        return args -> SwingUtilities.invokeLater(() -> shell.setVisible(true));
    }
}
