package org.biopipelinerunner.services;

import org.biopipelinerunner.models.AlignmentConfig;
import org.biopipelinerunner.utils.ProcessExecutionService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlignmentService {
    private static final Logger LOGGER = Logger.getLogger(AlignmentService.class.getName());
    private final ProcessExecutionService processExecutionService;
    private AlignmentConfig config;
    
    public AlignmentService(AlignmentConfig config) {
        this.processExecutionService = new ProcessExecutionService();
        this.config = config;
    }
    
    /**
     * Run sequence alignment with the specified configuration
     * 
     * @param config The alignment configuration to use
     * @throws IOException if an I/O error occurs during command execution
     * @throws InterruptedException if the command execution is interrupted
     */
    public void runAlignment(AlignmentConfig config) throws IOException, InterruptedException {
        this.config = config;
        String command = buildAlignmentCommand();
        
        LOGGER.info("Running alignment with command: " + command);
        try {
            String output = processExecutionService.executeCommand(command);
            LOGGER.info("Alignment completed successfully");
            LOGGER.fine(output);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error during alignment execution", e);
            throw e;
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Alignment process interrupted", e);
            Thread.currentThread().interrupt(); // Preserve interrupt status
            throw e;
        }
    }
    
    private String buildAlignmentCommand() {
        StringBuilder command = new StringBuilder();
        
        if ("mafft".equalsIgnoreCase(config.getAlignmentTool())) {
            command.append("mafft --auto --thread ")
                  .append(config.getThreads())
                  .append(" ")
                  .append(config.getInputPath())
                  .append(" > ")
                  .append(config.getOutputPath());
        } else if ("muscle".equalsIgnoreCase(config.getAlignmentTool())) {
            command.append("muscle -in ")
                  .append(config.getInputPath())
                  .append(" -out ")
                  .append(config.getOutputPath());
        } else {
            LOGGER.warning("Unknown alignment tool specified: " + config.getAlignmentTool() + ". Defaulting to MUSCLE.");
            command.append("muscle -in ")
                  .append(config.getInputPath())
                  .append(" -out ")
                  .append(config.getOutputPath());
        }
        
        return command.toString();
    }
}
