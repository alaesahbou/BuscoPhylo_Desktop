package org.biopipelinerunner.services;

import org.biopipelinerunner.models.BuscoConfig;
import org.biopipelinerunner.utils.PlatformUtils;
import org.biopipelinerunner.utils.ProcessExecutionService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BuscoService {
    private final ProcessExecutionService processExecutionService;
    private static final String[] AVAILABLE_LINEAGES = {
        "bacteria", "eukaryota", "archaea", "metazoa", "fungi", "embryophyta", "protists",
        "vertebrata", "arthropoda", "mollusca", "nematoda", "actinobacteria", "proteobacteria",
        "firmicutes", "cyanobacteria", "ascomycota", "basidiomycota", "eudicots", "monocots"
    };

    public BuscoService() {
        this.processExecutionService = new ProcessExecutionService();
    }

    public void runBusco(BuscoConfig buscoConfig) {
        String buscoCommand = buildBuscoCommand(buscoConfig);
        processExecutionService.executeCommand(buscoCommand);
    }

    private String buildBuscoCommand(BuscoConfig buscoConfig) {
        StringBuilder command = new StringBuilder();
        
        // Use busco script location if provided, otherwise just call busco from PATH
        String buscoExec = buscoConfig.getBuscoPath() != null && !buscoConfig.getBuscoPath().isEmpty() ?
                buscoConfig.getBuscoPath() : "busco";
        
        command.append(buscoExec)
              .append(" -i ").append(getPlatformPath(buscoConfig.getInputDirectory()))
              .append(" -o ").append(getPlatformPath(Paths.get(buscoConfig.getOutputDirectory()).getFileName().toString()))
              .append(" -m genome")  // Mode could be configurable: genome, proteins, transcriptome
              .append(" -l ").append(buscoConfig.getLineage() != null ? buscoConfig.getLineage() : "bacteria")
              .append(" --out-path ").append(getPlatformPath(Paths.get(buscoConfig.getOutputDirectory()).getParent().toString()))
              .append(" --cpu ").append(buscoConfig.getThreads() > 0 ? buscoConfig.getThreads() : Runtime.getRuntime().availableProcessors());

        return command.toString();
    }
    
    private String getPlatformPath(String path) {
        // If on Windows with WSL, convert the path to WSL format
        if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
            return PlatformUtils.convertWindowsPathToWsl(path);
        }
        return path;
    }

    public boolean checkBuscoInstallation() {
        try {
            String command;
            if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
                command = "busco --version";
            } else if (PlatformUtils.isWindows()) {
                command = "busco --version";
            } else {
                command = "busco --version";
            }
            processExecutionService.executeCommand(command);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String[] getAvailableLineages() {
        try {
            String output = processExecutionService.executeCommand("busco --list-datasets");
            // Parse output to extract lineages
            // This is a simplified version; actual implementation would parse the output
            return AVAILABLE_LINEAGES;
        } catch (Exception e) {
            return AVAILABLE_LINEAGES; // Return default list if command fails
        }
    }
}