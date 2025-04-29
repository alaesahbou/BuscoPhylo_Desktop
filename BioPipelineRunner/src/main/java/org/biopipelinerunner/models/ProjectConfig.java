package org.biopipelinerunner.models;

public class ProjectConfig {
    private String projectName;
    private String outputDirectory;

    public ProjectConfig(String projectName, String outputDirectory) {
        this.projectName = projectName;
        this.outputDirectory = outputDirectory;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}