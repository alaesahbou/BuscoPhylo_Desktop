package org.biopipelinerunner.services;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.biopipelinerunner.utils.CommandLineUtils;
import org.biopipelinerunner.utils.FileUtils;
import org.biopipelinerunner.utils.PlatformUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VisualizationService {
    private final ProcessExecutionService processExecutionService;
    
    public VisualizationService() {
        this.processExecutionService = new ProcessExecutionService();
    }

    public void displayTree(String treeFilePath, VBox container) {
        File treeFile = new File(treeFilePath);
        if (treeFile.exists()) {
            Image treeImage = new Image(treeFile.toURI().toString());
            ImageView imageView = new ImageView(treeImage);
            imageView.setFitWidth(container.getWidth() * 0.9);
            imageView.setPreserveRatio(true);
            container.getChildren().add(imageView);
        } else {
            System.err.println("Tree file not found: " + treeFilePath);
        }
    }

    public void displayResults(String resultsDirectory, VBox container) {
        File resultsDir = new File(resultsDirectory);
        if (resultsDir.exists() && resultsDir.isDirectory()) {
            for (File file : resultsDir.listFiles()) {
                if (file.getName().endsWith(".png") || file.getName().endsWith(".pdf")) {
                    displayFile(file, container);
                }
            }
        } else {
            System.err.println("Results directory not found: " + resultsDirectory);
        }
    }

    public boolean generateTreeVisualization(String treeFilePath, String outputDirectory, String outgroup) {
        try {
            Path scriptPath = Paths.get(System.getProperty("user.dir"), "scripts", "tree.py");
            File scriptFile = new File(scriptPath.toString());
            
            if (!scriptFile.exists()) {
                // Try to find the script in the resources
                String resourcePath = "/scripts/tree.py";
                File tempFile = File.createTempFile("tree", ".py");
                try {
                    FileUtils.copyResourceToFile(resourcePath, tempFile);
                    scriptFile = tempFile;
                } catch (IOException e) {
                    System.err.println("Could not find tree.py script: " + e.getMessage());
                    return false;
                }
            }
            
            String pythonExecutable = PlatformUtils.isWindows() && PlatformUtils.isWslInstalled() ? 
                    "python3" : (PlatformUtils.isWindows() ? "python" : "python3");
            
            String command;
            if (outgroup != null && !outgroup.isEmpty()) {
                command = pythonExecutable + " " + scriptFile.getAbsolutePath() + " " + outgroup;
            } else {
                command = pythonExecutable + " " + scriptFile.getAbsolutePath();
            }
            
            processExecutionService.executeCommand("cd " + outputDirectory + " && " + command);
            return true;
        } catch (Exception e) {
            System.err.println("Error generating tree visualization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void displayFile(File file, VBox container) {
        try {
            Image fileImage = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(fileImage);
            imageView.setFitWidth(container.getWidth() * 0.9);
            imageView.setPreserveRatio(true);
            container.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Error displaying file: " + file.getPath() + ": " + e.getMessage());
        }
    }
    
    public boolean visualizeResults(String resultsData) {
        // This would process result data and create visualizations
        return !resultsData.isEmpty();
    }
}