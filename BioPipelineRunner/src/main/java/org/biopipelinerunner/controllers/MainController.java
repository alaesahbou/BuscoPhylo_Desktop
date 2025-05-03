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
import org.biopipelinerunner.utils.PlatformUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
                    updateProgressBar(0.1);
                    
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
                    
                    try {
                        buscoService.runBusco(buscoConfig);
                    } catch (IOException | InterruptedException e) {
                        logError("BUSCO analysis failed: " + e.getMessage());
                        throw e;
                    }
                    updateProgressBar(0.4);
                    
                    // Run phylogenetic analysis
                    updateStatus("Running phylogenetic analysis...");
                    new File(outputDirPath).mkdirs();
                    
                    phylogenyService.runPhylogeneticAnalysis(
                        buscoOutDir,
                        outputDirPath,
                        Runtime.getRuntime().availableProcessors(),
                        outgroupField.getText()
                    );
                    updateProgressBar(0.7);
                    
                    // Generate tree visualization
                    updateStatus("Generating tree visualization...");
                    visualizationService.generateTreeVisualization(
                        outputDirPath + "/SUPERMATRIX.trimmed.aln.contree",
                        outputDirPath,
                        outgroupField.getText()
                    );
                    updateProgressBar(0.9);
                    
                    // Create results archive
                    updateStatus("Creating results archive...");
                    File resultsZip = new File(workingDirPath + "/" + projectName + "_results.zip");
                    FileUtils.zipDirectory(new File(outputDirPath), resultsZip);
                    updateProgressBar(1.0);
                    
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
    
    /**
     * Checks for required dependencies and offers to automatically install any missing ones.
     * This method is called from the FXML file.
     */
    @FXML
    public void checkDependencies() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                updateStatus("Checking dependencies...");
                Map<String, Boolean> dependencyStatus = DependencyManager.checkDependencies();
                
                boolean allDependenciesInstalled = dependencyStatus.values().stream().allMatch(Boolean::booleanValue);
                
                if (allDependenciesInstalled) {
                    updateStatus("All dependencies are installed correctly.");
                } else {
                    // Get missing dependencies
                    List<String> missingDeps = dependencyStatus.entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                    
                    Platform.runLater(() -> {
                        boolean install = showConfirmationDialog(
                            "Missing Dependencies", 
                            "Some required dependencies are missing: " + String.join(", ", missingDeps),
                            "Would you like to automatically install the missing dependencies?"
                        );
                        
                        if (install) {
                            installDependencies(missingDeps);
                        } else {
                            // Show installation instructions if user declines auto-install
                            String instructions = DependencyManager.getInstallationInstructions(dependencyStatus);
                            logTextArea.appendText("Missing dependencies detected. Please install them before running the pipeline.\n\n");
                            logTextArea.appendText(instructions);
                        }
                    });
                }
                
                return null;
            }
        };
        
        executorService.submit(task);
    }
    
    /**
     * Automatically install missing dependencies
     * 
     * @param missingDeps List of missing dependency names
     */
    private void installDependencies(List<String> missingDeps) {
        Task<Void> installTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateStatus("Installing missing dependencies...");
                
                // Separate system packages and Python packages
                List<String> systemPackages = new ArrayList<>();
                List<String> pythonPackages = new ArrayList<>();
                
                for (String dep : missingDeps) {
                    if (dep.startsWith("python") || dep.equals("pip")) {
                        systemPackages.add(dep);
                    } else if (Arrays.asList("biopython", "ete3").contains(dep)) {
                        pythonPackages.add(dep);
                    } else if (Arrays.asList("muscle", "trimal", "iqtree", "busco").contains(dep)) {
                        systemPackages.add(dep);
                    }
                }
                
                // Install system packages first
                if (!systemPackages.isEmpty()) {
                    installSystemPackages(systemPackages);
                }
                
                // Then install Python packages
                if (!pythonPackages.isEmpty()) {
                    installPythonPackages(pythonPackages);
                }
                
                // Verify installations
                updateStatus("Verifying installations...");
                Map<String, Boolean> updatedStatus = DependencyManager.checkDependencies();
                
                boolean allInstalled = updatedStatus.entrySet().stream()
                    .filter(entry -> missingDeps.contains(entry.getKey()))
                    .allMatch(entry -> entry.getValue());
                
                if (allInstalled) {
                    updateStatus("All dependencies successfully installed!");
                } else {
                    List<String> stillMissing = updatedStatus.entrySet().stream()
                        .filter(entry -> !entry.getValue() && missingDeps.contains(entry.getKey()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                    
                    updateStatus("Some dependencies could not be installed automatically: " + String.join(", ", stillMissing));
                    
                    Platform.runLater(() -> {
                        String manualInstructions = DependencyManager.getInstallationInstructionsForSpecificDeps(stillMissing);
                        logTextArea.appendText("\nPlease install these dependencies manually:\n");
                        logTextArea.appendText(manualInstructions);
                        
                        showAlert("Installation Incomplete", 
                            "Some dependencies require manual installation. See the log panel for instructions.");
                    });
                }
                
                return null;
            }
        };
        
        installTask.setOnFailed(event -> {
            Throwable e = installTask.getException();
            logError("Failed to install dependencies: " + (e != null ? e.getMessage() : "Unknown error"));
            e.printStackTrace();
        });
        
        executorService.submit(installTask);
    }
    
    /**
     * Install system packages based on the operating system
     * 
     * @param packages List of system packages to install
     * @throws IOException If an installation error occurs
     * @throws InterruptedException If the installation process is interrupted
     */
    private void installSystemPackages(List<String> packages) throws IOException, InterruptedException {
        if (packages.isEmpty()) return;
        
        String packagesStr = String.join(" ", packages);
        
        if (PlatformUtils.isLinux()) {
            updateStatus("Installing system packages with apt-get: " + packagesStr);
            executeCommand("sudo apt-get update");
            executeCommand("sudo apt-get install -y " + packagesStr);
        } else if (PlatformUtils.isMac()) {
            updateStatus("Installing system packages with brew: " + packagesStr);
            executeCommand("brew install " + packagesStr);
        } else if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
            updateStatus("Installing system packages in WSL: " + packagesStr);
            executeCommand("wsl sudo apt-get update");
            executeCommand("wsl sudo apt-get install -y " + packagesStr);
        } else if (PlatformUtils.isWindows()) {
            // For Windows, handle special cases
            updateStatus("Windows detected. Some packages may need manual installation.");
            
            // For Python on Windows
            if (packages.contains("python3") || packages.contains("python")) {
                updateStatus("Installing Python via winget...");
                executeCommand("winget install -e --id Python.Python.3.9");
            }
            
            // For other tools, download and install scripts could be added here
            // ...
        }
    }
    
    /**
     * Install Python packages using pip
     * 
     * @param packages List of Python packages to install
     * @throws IOException If an installation error occurs
     * @throws InterruptedException If the installation process is interrupted
     */
    private void installPythonPackages(List<String> packages) throws IOException, InterruptedException {
        if (packages.isEmpty()) return;
        
        String packagesStr = String.join(" ", packages);
        String pipCommand;
        
        if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
            updateStatus("Installing Python packages in WSL: " + packagesStr);
            pipCommand = "wsl pip install --user " + packagesStr;
        } else if (PlatformUtils.isWindows()) {
            updateStatus("Installing Python packages: " + packagesStr);
            pipCommand = "pip install --user " + packagesStr;
        } else {
            updateStatus("Installing Python packages: " + packagesStr);
            pipCommand = "pip3 install --user " + packagesStr;
        }
        
        executeCommand(pipCommand);
    }
    
    /**
     * Execute a command and log its output
     * 
     * @param command The command to execute
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    private void executeCommand(String command) throws IOException, InterruptedException {
        updateStatus("Executing: " + command);
        ProcessBuilder processBuilder;
        
        if (PlatformUtils.isWindows() && !command.startsWith("wsl")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else if (PlatformUtils.isWindows() && command.startsWith("wsl")) {
            // For WSL commands, split the command to execute properly
            processBuilder = new ProcessBuilder(command.split(" "));
        } else {
            processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        }
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // Read output in a separate thread to prevent blocking
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String outputLine = line;
                    Platform.runLater(() -> logTextArea.appendText(outputLine + "\n"));
                }
            } catch (IOException e) {
                Platform.runLater(() -> logError("Error reading process output: " + e.getMessage()));
            }
        });
        outputThread.start();
        
        // Wait for process to complete
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with exit code: " + exitCode);
        }
    }
    
    /**
     * Show a confirmation dialog to the user
     * 
     * @param title Dialog title
     * @param headerText Dialog header text
     * @param contentText Dialog content text
     * @return true if the user confirmed, false otherwise
     */
    private boolean showConfirmationDialog(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private void updateProgressBar(double progress) {
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
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
