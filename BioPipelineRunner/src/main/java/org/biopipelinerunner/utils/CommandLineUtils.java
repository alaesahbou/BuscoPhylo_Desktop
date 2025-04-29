package org.biopipelinerunner.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandLineUtils {

    public static String executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
            // Using WSL on Windows
            processBuilder.command("wsl.exe", "-e", "bash", "-c", command);
        } else if (PlatformUtils.isWindows()) {
            // Windows native command execution
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            // Linux/Mac command execution
            processBuilder.command("bash", "-c", command);
        }
        
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        // Handle standard output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // Handle error output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command execution failed with exit code: " + exitCode + "\nError: " + error.toString());
        }

        return output.toString().trim();
    }
    
    public static String executeCommandWithArgs(String... args) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> command = new ArrayList<>();
        
        if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
            // Convert Windows paths to WSL paths for arguments
            command.add("wsl.exe");
            command.add("-e");
            
            List<String> wslArgs = new ArrayList<>();
            for (String arg : args) {
                if (arg.contains(":\\") || arg.contains(":/")) {
                    wslArgs.add(PlatformUtils.convertWindowsPathToWsl(arg));
                } else {
                    wslArgs.add(arg);
                }
            }
            command.addAll(wslArgs);
        } else {
            command.addAll(Arrays.asList(args));
        }
        
        processBuilder.command(command);
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        // Handle standard output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // Handle error output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command execution failed with exit code: " + exitCode + "\nError: " + error.toString());
        }

        return output.toString().trim();
    }
}