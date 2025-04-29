package org.biopipelinerunner.services;

import org.biopipelinerunner.utils.CommandLineUtils;

import java.io.IOException;

public class ProcessExecutionService {
    private final CommandLineUtils commandLineUtils;
    
    public ProcessExecutionService(CommandLineUtils commandLineUtils) {
        this.commandLineUtils = commandLineUtils;
    }
    
    public ProcessExecutionService() {
        this.commandLineUtils = new CommandLineUtils();
    }

    public String executeCommand(String command) {
        try {
            return CommandLineUtils.executeCommand(command);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error executing command: " + command, e);
        }
    }
    
    public String executeCommandWithArgs(String... args) {
        try {
            return CommandLineUtils.executeCommandWithArgs(args);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error executing command: " + String.join(" ", args), e);
        }
    }
    
    public boolean isProcessAvailable(String processName) {
        try {
            String command;
            if (org.biopipelinerunner.utils.PlatformUtils.isWindows()) {
                command = "where " + processName;
            } else {
                command = "which " + processName;
            }
            executeCommand(command);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}