package org.biopipelinerunner.models;

public class PhylogenyConfig {
    private String inputPath;
    private String outputPath;
    private int threads;
    private String outgroup;
    private String phylogenyTool; // e.g., "iqtree", "raxml"
    private String phyloScriptPath; // Added missing property
    
    public PhylogenyConfig() {
        this.threads = Runtime.getRuntime().availableProcessors();
        this.phylogenyTool = "iqtree";
    }
    
    public PhylogenyConfig(String inputPath, String outputPath, int threads, String outgroup, String phylogenyTool) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.threads = threads;
        this.outgroup = outgroup;
        this.phylogenyTool = phylogenyTool;
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
    
    public String getOutgroup() {
        return outgroup;
    }
    
    public void setOutgroup(String outgroup) {
        this.outgroup = outgroup;
    }
    
    public String getPhylogenyTool() {
        return phylogenyTool;
    }
    
    public void setPhylogenyTool(String phylogenyTool) {
        this.phylogenyTool = phylogenyTool;
    }
    
    public String getPhyloScriptPath() {
        return phyloScriptPath;
    }
    
    public void setPhyloScriptPath(String phyloScriptPath) {
        this.phyloScriptPath = phyloScriptPath;
    }
}
