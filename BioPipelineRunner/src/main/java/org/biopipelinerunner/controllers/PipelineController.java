package org.biopipelinerunner.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.biopipelinerunner.models.AlignmentConfig;
import org.biopipelinerunner.models.BuscoConfig;
import org.biopipelinerunner.models.PhylogenyConfig;
import org.biopipelinerunner.services.BuscoService;
import org.biopipelinerunner.services.AlignmentService;
import org.biopipelinerunner.services.PhylogenyService;
import org.biopipelinerunner.utils.DependencyManager;

import java.io.File;
import java.io.IOException;

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
        this.alignmentService = new AlignmentService(new AlignmentConfig());
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
            
            // Create a BuscoConfig object with the input
            BuscoConfig buscoConfig = new BuscoConfig(
                null,  // Use system BUSCO
                buscoInput,
                buscoInput + File.separator + "busco_output",
                Runtime.getRuntime().availableProcessors(),
                "" // No outgroup specified
            );
            
            try {
                buscoService.runBusco(buscoConfig);
                
                // Create appropriate config objects for alignment and phylogeny
                AlignmentConfig alignmentConfig = new AlignmentConfig();
                alignmentConfig.setInputPath(alignmentInput);
                alignmentService.runAlignment(alignmentConfig);
                
                PhylogenyConfig phylogenyConfig = new PhylogenyConfig();
                phylogenyConfig.setInputPath(phylogenyInput);
                phylogenyService.runPhylogeneticAnalysis(
                    phylogenyInput,
                    phylogenyInput + File.separator + "output",
                    Runtime.getRuntime().availableProcessors(),
                    "" // No outgroup specified
                );
            } catch (IOException | InterruptedException e) {
                // Handle the exceptions - could show dialog, log error, etc.
                System.err.println("Error executing pipeline: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Handle invalid inputs (e.g., show an error message)
        }
    }

    private boolean validateInputs(String buscoInput, String alignmentInput, String phylogenyInput) {
        return !buscoInput.isEmpty() && !alignmentInput.isEmpty() && !phylogenyInput.isEmpty();
    }
}
