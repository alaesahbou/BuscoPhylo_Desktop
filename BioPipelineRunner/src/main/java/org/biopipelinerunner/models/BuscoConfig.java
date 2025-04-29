package org.biopipelinerunner.models;

public class BuscoConfig {
    private String buscoPath;
    private String inputDirectory;
    private String outputDirectory;
    private int threads;
    private String outgroup;
    private String lineage;
    private String mode;

    public BuscoConfig(String buscoPath, String inputDirectory, String outputDirectory, int threads, String outgroup) {
        this.buscoPath = buscoPath;
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.threads = threads;
        this.outgroup = outgroup;
        this.lineage = "bacteria"; // Default lineage
        this.mode = "genome";      // Default mode
    }

    // Getters and setters
    public String getBuscoPath() {
        return buscoPath;
    }

    public void setBuscoPath(String buscoPath) {
        this.buscoPath = buscoPath;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getOutgroup() {
        return outgroup;
    }

    public void setOutgroup(String outgroup) {
        this.outgroup = outgroup;
    }
    
    public String getLineage() {
        return lineage;
    }
    
    public void setLineage(String lineage) {
        this.lineage = lineage;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
}