package org.biopipelinerunner.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biopipelinerunner.models.PhylogenyConfig;
import org.biopipelinerunner.utils.FileUtils;
import org.biopipelinerunner.utils.PlatformUtils;
import org.biopipelinerunner.utils.ProcessExecutionService;

public class PhylogenyService {
    private static final Logger LOGGER = Logger.getLogger(PhylogenyService.class.getName());
    private PhylogenyConfig config;
    private final ProcessExecutionService processExecutionService;

    public PhylogenyService() {
        this.processExecutionService = new ProcessExecutionService();
    }

    public PhylogenyService(PhylogenyConfig config) {
        this.config = config;
        this.processExecutionService = new ProcessExecutionService();
    }

    /**
     * Run a phylogenetic analysis using the provided configuration
     * 
     * @param inputDirectory Directory containing BUSCO results
     * @param outputDirectory Directory to save results
     * @param threads Number of threads to use
     * @param outgroup Outgroup for tree rooting (can be null)
     * @throws IOException if file operations fail
     * @throws InterruptedException if process execution is interrupted
     */
    public void runPhylogeneticAnalysis(String inputDirectory, String outputDirectory, int threads, String outgroup) 
            throws IOException, InterruptedException {
        LOGGER.info("Starting phylogenetic analysis");
        LOGGER.info("Input directory: " + inputDirectory);
        LOGGER.info("Output directory: " + outputDirectory);
        LOGGER.info("Threads: " + threads);
        if (outgroup != null && !outgroup.isEmpty()) {
            LOGGER.info("Outgroup: " + outgroup);
        }
        
        // Create output directory if it doesn't exist
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create output directory: " + outputDirectory);
            }
        }
        
        // Ensure script_phylo.py is available
        Path scriptPath = Paths.get(System.getProperty("user.dir"), "scripts", "script_phylo.py");
        File scriptFile = new File(scriptPath.toString());
        
        if (!scriptFile.exists()) {
            // Try to find the script in the resources
            String resourcePath = "/scripts/script_phylo.py";
            File tempFile = File.createTempFile("script_phylo", ".py");
            tempFile.deleteOnExit();
            
            try {
                FileUtils.copyResourceToFile(resourcePath, tempFile);
                scriptFile = tempFile;
                LOGGER.info("Using script from resources: " + tempFile.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not find script_phylo.py script", e);
                throw new IOException("Could not find script_phylo.py script: " + e.getMessage(), e);
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
        LOGGER.info("Executing phylogeny command: " + command);
        
        try {
            String output = processExecutionService.executeCommand(command);
            LOGGER.info("Phylogenetic analysis completed successfully");
            LOGGER.fine(output);
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error running phylogenetic analysis", e);
            throw e;
        }
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
        
        if (config == null || config.getPhyloScriptPath() == null) {
            LOGGER.warning("PhylogenyConfig or script path is null");
            return null;
        }
        
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
    
    /**
     * Get the path to the phylogeny script
     * @return Script path or null if not set
     */
    public String getPhyloScriptPath() {
        return config != null ? config.getPhyloScriptPath() : null;
    }
    
    /**
     * Set the phylogeny configuration
     * @param config PhylogenyConfig object
     */
    public void setConfig(PhylogenyConfig config) {
        this.config = config;
    }
    
    /**
     * Get the current phylogeny configuration
     * @return Current PhylogenyConfig
     */
    public PhylogenyConfig getConfig() {
        return this.config;
    }
}