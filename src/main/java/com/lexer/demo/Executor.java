package com.lexer.demo;

import java.io.*;
import java.util.*;
import org.springframework.stereotype.Component;

import com.lexer.demo.Utils.BuiltinCommands;

@Component
public class Executor {

    private File currentDirectory = new File(System.getProperty("user.dir"));

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    private File canonical(File dir) {
        try {
            return dir.getCanonicalFile();
        } catch (IOException e) {
            return dir.getAbsoluteFile();
        }
    }

    public String executeAndCapture(PipelineNode pipeline) {
        // Only one command in pipeline -> try built-ins first
        if (pipeline.commands.size() == 1) {
            CommandNode cmd = pipeline.commands.get(0);

            if (cmd.name.startsWith("cd") && !cmd.name.equals("cd")) {
                return "cd: invalid syntax\n";
            }

            // Handle cd / pwd / clear
            if (cmd.name.equals("cd")) return handleCd(cmd);
            if (cmd.name.equals("pwd")) return currentDirectory.getAbsolutePath() + "\n";
            if (cmd.name.equals("clear")) return "__CLEAR__";

            // Handle custom built-ins: ls, rm
            String builtin = BuiltinCommands.execute(cmd.name + concatArgs(cmd.args), this);
            if (builtin != null) return builtin;
        }

        // ---- EXTERNAL COMMANDS ----
        StringBuilder output = new StringBuilder();
        List<Process> processes = new ArrayList<>();
        Process previous = null;

        try {
            for (int i = 0; i < pipeline.commands.size(); i++) {
                CommandNode cmd = pipeline.commands.get(i);

                List<String> command = new ArrayList<>();
                command.add(cmd.name);
                command.addAll(cmd.args);

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(currentDirectory);
                pb.redirectErrorStream(true);

                Process process = pb.start();

                if (previous != null) pipe(previous.getInputStream(), process.getOutputStream());

                if (i == pipeline.commands.size() - 1 && cmd.redirectOut == null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                    }
                }

                processes.add(process);
                previous = process;

                if (cmd.background && i == pipeline.commands.size() - 1) return "[running in background]\n";
            }

            for (Process p : processes) p.waitFor();

        } catch (IOException | InterruptedException e) {
            return "command not found\n";
        }

        return output.toString();
    }

    private String handleCd(CommandNode cmd) {
        File target;
        if (cmd.args.isEmpty()) target = new File(System.getProperty("user.home"));
        else target = new File(currentDirectory, cmd.args.get(0));

        target = canonical(target);
        if (!target.exists() || !target.isDirectory()) return "cd: no such directory\n";

        currentDirectory = target;
        return "";
    }

    private void pipe(InputStream in, OutputStream out) {
        new Thread(() -> {
            try (in; out) {
                in.transferTo(out);
            } catch (IOException ignored) {}
        }).start();
    }

    private String concatArgs(List<String> args) {
        if (args.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String a : args) sb.append(" ").append(a);
        return sb.toString();
    }
}