package com.lexer.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/shell")
public class ShellController {

    private final Executor executor = new Executor();

    @GetMapping
    public ResponseEntity<String> runCommand(@RequestParam String input) {
        try {
            // 1️⃣ Lex
            Lexer lexer = new Lexer(input);
            var tokens = lexer.tokenize();

            // 2️⃣ Parse
            Parser parser = new Parser(tokens);
            PipelineNode ast = parser.parse();

            // 3️⃣ Execute and capture output
            String output = executor.executeAndCapture(ast);

            return ResponseEntity.ok(output);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}

