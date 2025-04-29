package org.biopipelinerunner.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.biopipelinerunner.models.BuscoConfig;
import org.biopipelinerunner.services.BuscoService;
import org.biopipelinerunner.services.PhylogenyService;
import org.biopipelinerunner.services.VisualizationService;
import org.biopipelinerunner.utils.DependencyManager;
import org.biopipelinerunner.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML
    private VBox mainContainer;

    @FXML
    private TextField inputField;

    @FXML
    private Button runPipelineButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;
    
    @FXML
    private TextArea logTextArea;
    
    @FXML
    private TextField projectNameField;
    
    @FXML
    private TextField outgroupField;
    
    @FXML
    private ComboBox<String> buscoLineageComboBox;
    
    @FXML
    private ComboBox<String> buscoModeComboBox;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final BuscoService buscoService = new BuscoService();
    private final PhylogenyService phylogenyService = new PhylogenyService();
    private final VisualizationService visualizationService = new VisualizationService();

    @FXML
    public void initialize() {
        // Initialize UI components
        progressBar.setProgress(0);
        statusLabel.setText("Ready");
        
        // Set up ComboBox items
        buscoLineageComboBox.getItems().addAll(buscoService.getAvailableLineages());
        buscoLineageComboBox.setValue("bacteria");
        
        buscoModeComboBox.getItems().addAll("genome", "proteins", "transcriptome");
        buscoModeComboBox.setValue("genome");
        
        // Button actions
        runPipelineButton.setOnAction(event -> handleRunPipeline());
        
        // Check dependencies on startup
        checkDependencies();
    }

    @FXML
    private void handleRunPipeline() {
        // Validate inputs
        if (!validateInputs()) {
            showAlert("Invalid Input", "Please check all required fields.");
            return;
        }
        
        // Disable the run button while pipeline is running
        runPipelineButton.setDisable(true);
        
        // Create working directory
        String projectName = projectNameField.getText();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String workingDirPath = System.getProperty("user.home") + "/BioPipelineRunner/" + projectName + "_" + timestamp;
        File workingDir = new File(workingDirPath);
        
        try {
            FileUtils.createDirectory(workingDir);
        } catch (Exception e) {
            logError("Failed to create working directory: " + e.getMessage());
            runPipelineButton.setDisable(false);
            return;
        }
        
        // Final output directory path for later use
        String outputDirPath = workingDirPath + "/output";
        
        // Create and start the task
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Set status and progress
                    updateStatus("Starting pipeline...");
                    updateProgress(0.1);
                    
                    // Run BUSCO analysis
                    updateStatus("Running BUSCO analysis...");
                    String inputDirPath = inputField.getText();
                    String buscoOutDir = workingDirPath + "/busco_output";
                    
                    BuscoConfig buscoConfig = new BuscoConfig(
                        null,  // Use system BUSCO
                        inputDirPath,
                        buscoOutDir,
                        Runtime.getRuntime().availableProcessors(),
                        outgroupField.getText()
                    );
                    buscoConfig.setLineage(buscoLineageComboBox.getValue());
                    buscoConfig.setMode(buscoModeComboBox.getValue());
                    
                    buscoService.runBusco(buscoConfig);
                    updateProgress(0.4);
                    
                    // Run phylogenetic analysis
                    updateStatus("Running phylogenetic analysis...");
                    new File(outputDirPath).mkdirs();
                    
                    phylogenyService.runPhylogeneticAnalysis(
                        buscoOutDir,
                        outputDirPath,
                        Runtime.getRuntime().availableProcessors(),
                        outgroupField.getText()
                    );
                    updateProgress(0.7);
                    
                    // Generate tree visualization
                    updateStatus("Generating tree visualization...");
                    visualizationService.generateTreeVisualization(
                        outputDirPath + "/SUPERMATRIX.trimmed.aln.contree",
                        outputDirPath,
                        outgroupField.getText()
                    );
                    updateProgress(0.9);
                    
                    // Create results archive
                    updateStatus("Creating results archive...");
                    File resultsZip = new File(workingDirPath + "/" + projectName + "_results.zip");
                    FileUtils.zipDirectory(new File(outputDirPath), resultsZip);
                    updateProgress(1.0);
                    
                    updateStatus("Pipeline completed successfully!");
                    return null;
                } catch (Exception e) {
                    updateStatus("Pipeline failed: " + e.getMessage());
                    logError(e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };
        
        // Handle task completion
        task.setOnSucceeded(event -> {
            runPipelineButton.setDisable(false);
        });
        
        task.setOnFailed(event -> {
            logError("Pipeline execution failed: " + task.getException().getMessage());
            runPipelineButton.setDisable(false);
        });
        
        // Start the task
        executorService.submit(task);
    }
    
    @FXML
    private void handleSelectInputDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Input Directory");
        File selectedDirectory = directoryChooser.showDialog(mainContainer.getScene().getWindow());
        
        if (selectedDirectory != null) {
            inputField.setText(selectedDirectory.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleViewResults() {
        // Implementation for viewing results
        // This could open a new window with the ResultsController
    }
    
    private void updateProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }
    
    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            logTextArea.appendText("[" + new Date() + "] " + message + "\n");
        });
    }
    
    private void logError(String error) {
        Platform.runLater(() -> {
            logTextArea.appendText("[ERROR] " + error + "\n");
        });
    }
    
    private boolean validateInputs() {
        String projectName = projectNameField.getText();
        String inputDirectory = inputField.getText();
        
        if (projectName == null || projectName.isEmpty() || projectName.contains(" ")) {
            return false;
        }
        
        if (inputDirectory == null || inputDirectory.isEmpty()) {
            return false;
        }
        
        File inputDir = new File(inputDirectory);
        return inputDir.exists() && inputDir.isDirectory();
    }
    
    private void checkDependencies() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                updateStatus("Checking dependencies...");
                Map<String, Boolean> dependencyStatus = DependencyManager.checkDependencies();
                
                boolean allDependenciesInstalled = dependencyStatus.values().stream().allMatch(Boolean::booleanValue);
                
                if (allDependenciesInstalled) {
                    updateStatus("All dependencies are installed correctly.");
                } else {
                    String instructions = DependencyManager.getInstallationInstructions(dependencyStatus);
                    Platform.runLater(() -> {
                        logTextArea.appendText("Missing dependencies detected. Please install them before running the pipeline.\n\n");
                        logTextArea.appendText(instructions);
                        
                        showAlert("Missing Dependencies", 
                                "Some required dependencies are missing. Check the log panel for installation instructions.");
                    });
                }
                
                return null;
            }
        };
        
        executorService.submit(task);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}