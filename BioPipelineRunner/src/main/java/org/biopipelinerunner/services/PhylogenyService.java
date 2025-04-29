package org.biopipelinerunner.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.biopipelinerunner.models.PhylogenyConfig;
import org.biopipelinerunner.utils.CommandLineUtils;
import org.biopipelinerunner.utils.FileUtils;
import org.biopipelinerunner.utils.PlatformUtils;

public class PhylogenyService {

    private PhylogenyConfig config;
    private final ProcessExecutionService processExecutionService;

    public PhylogenyService() {
        this.processExecutionService = new ProcessExecutionService();
    }

    public PhylogenyService(PhylogenyConfig config) {
        this.config = config;
        this.processExecutionService = new ProcessExecutionService();
    }

    public void runPhylogeneticAnalysis(String inputDirectory, String outputDirectory, int threads, String outgroup) throws IOException, InterruptedException {
        // Ensure script_phylo.py is in the resources
        Path scriptPath = Paths.get(System.getProperty("user.dir"), "scripts", "script_phylo.py");
        File scriptFile = new File(scriptPath.toString());
        
        if (!scriptFile.exists()) {
            // Try to find the script in the resources
            String resourcePath = "/scripts/script_phylo.py";
            File tempFile = File.createTempFile("script_phylo", ".py");
            try {
                FileUtils.copyResourceToFile(resourcePath, tempFile);
                scriptFile = tempFile;
            } catch (IOException e) {
                System.err.println("Could not find script_phylo.py script: " + e.getMessage());
                throw e;
            }
        }
        
        // Build the command
        StringBuilder commandBuilder = new StringBuilder();
        String pythonExec = PlatformUtils.isWindows() ? "python" : "python3";
        
        commandBuilder.append(pythonExec)
                     .append(" ")
                     .append(getPlatformPath(scriptFile.getAbsolutePath()))
                     .append(" -t ")
                     .append(threads)
                     .append(" -d ")
                     .append(getPlatformPath(inputDirectory))
                     .append(" -o ")
                     .append(getPlatformPath(outputDirectory));
        
        if (outgroup != null && !outgroup.isEmpty()) {
            commandBuilder.append(" -og ").append(outgroup);
        }
        
        String command = commandBuilder.toString();
        processExecutionService.executeCommand(command);
    }
    
    private String getPlatformPath(String path) {
        // If on Windows with WSL, convert the path to WSL format
        if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
            return PlatformUtils.convertWindowsPathToWsl(path);
        }
        return path;
    }

    private String buildCommand(List<File> inputFiles) {
        StringBuilder commandBuilder = new StringBuilder();
        String pythonExec = PlatformUtils.isWindows() ? "python" : "python3";
        
        commandBuilder.append(pythonExec)
                     .append(" ")
                     .append(config.getPhyloScriptPath())
                     .append(" ");

        for (File file : inputFiles) {
            commandBuilder.append(getPlatformPath(file.getAbsolutePath())).append(" ");
        }

        if (config.getOutgroup() != null && !config.getOutgroup().isEmpty()) {
            commandBuilder.append("-og ").append(config.getOutgroup());
        }

        return commandBuilder.toString().trim();
    }
    
    public String getPhyloScriptPath() {
        return config != null ? config.getPhyloScriptPath() : null;
    }
}