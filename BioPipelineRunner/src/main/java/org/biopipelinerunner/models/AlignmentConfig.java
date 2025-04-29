package org.biopipelinerunner.models;

public class AlignmentConfig {
    private String alignmentTool;
    private int threads;
    private String outputDirectory;

    public AlignmentConfig() {
        // Default constructor
    }

    public String getAlignmentTool() {
        return alignmentTool;
    }

    public void setAlignmentTool(String alignmentTool) {
        this.alignmentTool = alignmentTool;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}