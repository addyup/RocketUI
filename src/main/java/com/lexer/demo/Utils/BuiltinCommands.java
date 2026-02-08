package com.lexer.demo.Utils;

import java.io.File;
import java.util.Arrays;

import com.lexer.demo.Executor;

public class BuiltinCommands {

    public static String execute(String commandLine, Executor executor) {
        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length == 0) return "";

        String cmd = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        switch (cmd) {
            case "ls":
                return ls(args, executor);
            case "rm":
                return rm(args, executor);
            default:
                return null; // not a builtin
        }
    }

    // ---------------- LS ----------------
    private static String ls(String[] args, Executor executor) {
        File dir = executor.getCurrentDirectory();
        boolean longFormat = Arrays.asList(args).contains("-l");

        File[] files = dir.listFiles();
        if (files == null) return "";

        StringBuilder out = new StringBuilder();

        for (File f : files) {
            if (longFormat) {
                out.append(String.format("%s\t%s\t%d bytes\n",
                        f.isDirectory() ? "d" : "-",
                        f.getName(),
                        f.length()));
            } else {
                out.append(f.getName()).append("\n");
            }
        }

        return out.toString();
    }

    // ---------------- RM ----------------
    private static String rm(String[] args, Executor executor) {
        if (args.length == 0) return "rm: missing operand\n";

        boolean recursive = Arrays.asList(args).contains("-r");
        String targetName = args[args.length - 1];

        File target = new File(executor.getCurrentDirectory(), targetName);
        if (!target.exists()) return "rm: file not found\n";

        if (target.isDirectory() && !recursive) {
            return "rm: cannot remove directory (use -r)\n";
        }

        deleteRecursive(target);
        return "";
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }
}
