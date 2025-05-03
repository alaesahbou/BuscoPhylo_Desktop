package org.biopipelinerunner.models;

public class AlignmentConfig {
    private String inputPath;
    private String outputPath;
    private int threads;
    private String alignmentTool; // e.g., "mafft", "muscle"
    
    public AlignmentConfig() {
        this.threads = Runtime.getRuntime().availableProcessors();
        this.alignmentTool = "mafft";
    }
    
    public AlignmentConfig(String inputPath, String outputPath, int threads, String alignmentTool) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.threads = threads;
        this.alignmentTool = alignmentTool;
    }
    
    public String getInputPath() {
        return inputPath;
    }
    
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public int getThreads() {
        return threads;
    }
    
    public void setThreads(int threads) {
        this.threads = threads;
    }
    
    public String getAlignmentTool() {
        return alignmentTool;
    }
    
    public void setAlignmentTool(String alignmentTool) {
        this.alignmentTool = alignmentTool;
    }
}
