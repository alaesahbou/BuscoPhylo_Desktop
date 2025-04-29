package org.biopipelinerunner.services;

import org.biopipelinerunner.models.AlignmentConfig;
import org.biopipelinerunner.utils.CommandLineUtils;

import java.io.File;

public class AlignmentService {

    private AlignmentConfig alignmentConfig;

    public AlignmentService(AlignmentConfig alignmentConfig) {
        this.alignmentConfig = alignmentConfig;
    }

    public void runMuscle(File inputFile, File outputFile) throws Exception {
        String command = String.format("muscle -in %s -out %s", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
        CommandLineUtils.executeCommand(command);
    }

    public void runTrimal(File inputFile, File outputFile) throws Exception {
        String command = String.format("trimal -in %s -out %s -automated1", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
        CommandLineUtils.executeCommand(command);
    }

    public AlignmentConfig getAlignmentConfig() {
        return alignmentConfig;
    }

    public void setAlignmentConfig(AlignmentConfig alignmentConfig) {
        this.alignmentConfig = alignmentConfig;
    }
}