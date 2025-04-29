package org.biopipelinerunner.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.biopipelinerunner.services.BuscoService;
import org.biopipelinerunner.services.AlignmentService;
import org.biopipelinerunner.services.PhylogenyService;
import org.biopipelinerunner.utils.DependencyManager;

public class PipelineController {

    @FXML
    private TextField buscoInputField;

    @FXML
    private TextField alignmentInputField;

    @FXML
    private TextField phylogenyInputField;

    @FXML
    private Button runPipelineButton;

    private final BuscoService buscoService;
    private final AlignmentService alignmentService;
    private final PhylogenyService phylogenyService;

    public PipelineController() {
        this.buscoService = new BuscoService();
        this.alignmentService = new AlignmentService();
        this.phylogenyService = new PhylogenyService();
    }

    @FXML
    private void initialize() {
        runPipelineButton.setOnAction(event -> runPipeline());
    }

    private void runPipeline() {
        String buscoInput = buscoInputField.getText();
        String alignmentInput = alignmentInputField.getText();
        String phylogenyInput = phylogenyInputField.getText();

        if (validateInputs(buscoInput, alignmentInput, phylogenyInput)) {
            DependencyManager.checkDependencies();
            buscoService.runBusco(buscoInput);
            alignmentService.runAlignment(alignmentInput);
            phylogenyService.runPhylogeny(phylogenyInput);
        } else {
            // Handle invalid inputs (e.g., show an error message)
        }
    }

    private boolean validateInputs(String buscoInput, String alignmentInput, String phylogenyInput) {
        return !buscoInput.isEmpty() && !alignmentInput.isEmpty() && !phylogenyInput.isEmpty();
    }
}