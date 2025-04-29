# Busco Phylo Desktop

BioPipelineRunner is a JavaFX-based desktop application designed to streamline bioinformatics workflows. This application provides a user-friendly interface for managing the execution of various bioinformatics tasks, including BUSCO analysis, sequence alignment, phylogenetic tree construction, and result visualization.

## Features

- **Pipeline Management**: Easily configure and execute bioinformatics pipelines.
- **Dependency Management**: Automatically checks for required dependencies and provides installation guidance.
- **Result Visualization**: Visualize phylogenetic trees and other outputs in an intuitive manner.
- **Cross-Platform Support**: Designed to work seamlessly across different operating systems.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven for dependency management and building the project
- Required bioinformatics tools (e.g., BUSCO, MUSCLE, IQ-TREE) installed and accessible in your system's PATH

### Installation

1. Clone the repository:
   ```
   git clone <repository-url>
   cd BioPipelineRunner
   ```

2. Build the project using Maven:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn javafx:run
   ```

### Usage

1. Launch the application.
2. Navigate through the main view to configure your bioinformatics pipeline.
3. Start the execution of the pipeline and monitor the progress.
4. View and visualize the results once the execution is complete.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.
