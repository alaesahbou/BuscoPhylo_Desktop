module BioPipelineRunner {
    requires javafx.controls;
    requires javafx.fxml;
    requires ete3; // Assuming ete3 is available as a module
    requires org.apache.commons.io; // For file handling utilities
    requires org.slf4j; // For logging utilities

    exports org.biopipelinerunner;
    exports org.biopipelinerunner.controllers;
    exports org.biopipelinerunner.models;
    exports org.biopipelinerunner.services;
    exports org.biopipelinerunner.utils;
}