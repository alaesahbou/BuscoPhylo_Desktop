package org.biopipelinerunner.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.biopipelinerunner.services.VisualizationService;

public class ResultsController {

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private Button visualizeButton;

    @FXML
    private Label statusLabel;

    private VisualizationService visualizationService;

    public ResultsController() {
        this.visualizationService = new VisualizationService();
    }

    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    @FXML
    private void handleVisualizeButtonAction() {
        String results = resultsTextArea.getText();
        if (results.isEmpty()) {
            statusLabel.setText("No results to visualize.");
            return;
        }
        boolean success = visualizationService.visualizeResults(results);
        if (success) {
            statusLabel.setText("Visualization completed successfully.");
        } else {
            statusLabel.setText("Error during visualization.");
        }
    }

    public void setResults(String results) {
        resultsTextArea.setText(results);
    }
}