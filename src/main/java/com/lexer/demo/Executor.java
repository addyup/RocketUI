package com.lexer.demo;

import java.io.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class Executor {

    // Track current directory
    private File currentDirectory =
            new File(System.getProperty("user.dir"));

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Resolve . and .. cleanly
     */
    private File canonical(File dir) {
        try {
            return dir.getCanonicalFile();
        } catch (IOException e) {
            return dir.getAbsoluteFile();
        }
    }

    /**
     * Main execution entry
     */
    public String executeAndCapture(PipelineNode pipeline)
            throws InterruptedException {
    	// Guard against malformed cd like "cd.."
    	if (pipeline.commands.size() == 1) {
    	    CommandNode cmd = pipeline.commands.get(0);

    	    if (cmd.name.startsWith("cd") && !cmd.name.equals("cd")) {
    	        return "cd: invalid syntax\n";
    	    }
    	}

        // ---- BUILT-INS ----
        if (pipeline.commands.size() == 1) {
            CommandNode cmd = pipeline.commands.get(0);

            if (cmd.name.equals("cd")) {
                return handleCd(cmd);
            }

            if (cmd.name.equals("pwd")) {
                return currentDirectory.getAbsolutePath() + "\n";
            }

            if (cmd.name.equals("clear")) {
                return "__CLEAR__";
            }
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

                // Pipe previous → current
                if (previous != null) {
                    pipe(previous.getInputStream(), process.getOutputStream());
                }

                // Capture output of last command
                if (i == pipeline.commands.size() - 1 && cmd.redirectOut == null) {
                    try (BufferedReader reader =
                                 new BufferedReader(
                                     new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                    }
                }

                processes.add(process);
                previous = process;

                // Background execution
                if (cmd.background && i == pipeline.commands.size() - 1) {
                    return "[running in background]\n";
                }
            }

            for (Process p : processes) {
                p.waitFor();
            }

        } catch (IOException e) {
            // 🔥 THIS IS THE IMPORTANT PART
            return "command not found\n";
        }

        return output.toString();
    }

    /**
     * cd implementation (supports cd, cd .., cd ../..)
     */
    private String handleCd(CommandNode cmd) {

        File target;

        if (cmd.args.isEmpty()) {
            target = new File(System.getProperty("user.home"));
        } else {
            target = new File(currentDirectory, cmd.args.get(0));
        }

        target = canonical(target);

        if (!target.exists() || !target.isDirectory()) {
            return "cd: no such directory\n";
        }

        currentDirectory = target;
        return "";
    }

    /**
     * Pipe streams for pipelines
     */
    private void pipe(InputStream in, OutputStream out) {
        new Thread(() -> {
            try (in; out) {
                in.transferTo(out);
            } catch (IOException ignored) {}
        }).start();
    }
}
