package org.biopipelinerunner.models;

public class PhylogenyConfig {
    private String alignmentFilePath;
    private String outgroup;
    private int bootstrapReplicates;
    private String phyloScriptPath;

    public PhylogenyConfig(String alignmentFilePath, String outgroup, int bootstrapReplicates) {
        this.alignmentFilePath = alignmentFilePath;
        this.outgroup = outgroup;
        this.bootstrapReplicates = bootstrapReplicates;
    }

    public String getAlignmentFilePath() {
        return alignmentFilePath;
    }

    public void setAlignmentFilePath(String alignmentFilePath) {
        this.alignmentFilePath = alignmentFilePath;
    }

    public String getOutgroup() {
        return outgroup;
    }

    public void setOutgroup(String outgroup) {
        this.outgroup = outgroup;
    }

    public int getBootstrapReplicates() {
        return bootstrapReplicates;
    }

    public void setBootstrapReplicates(int bootstrapReplicates) {
        this.bootstrapReplicates = bootstrapReplicates;
    }
    
    public String getPhyloScriptPath() {
        return phyloScriptPath;
    }
    
    public void setPhyloScriptPath(String phyloScriptPath) {
        this.phyloScriptPath = phyloScriptPath;
    }
}