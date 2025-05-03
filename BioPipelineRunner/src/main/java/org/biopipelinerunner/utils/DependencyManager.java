package org.biopipelinerunner.utils;

import java.io.IOException;
import java.util.*;

public class DependencyManager {
    private static final List<String> REQUIRED_DEPENDENCIES = Arrays.asList(
        "python3", "pip", "muscle", "trimal", "iqtree", "raxmlHPC-PTHREADS"
    );
    
    private static final List<String> REQUIRED_PYTHON_PACKAGES = Arrays.asList(
        "biopython", "ete3"
    );

    public static Map<String, Boolean> checkDependencies() {
        Map<String, Boolean> dependencyStatus = new HashMap<>();
        
        for (String dependency : REQUIRED_DEPENDENCIES) {
            dependencyStatus.put(dependency, isDependencyInstalled(dependency));
        }
        
        if (dependencyStatus.getOrDefault("python3", false)) {
            for (String pyPackage : REQUIRED_PYTHON_PACKAGES) {
                dependencyStatus.put(pyPackage, isPythonPackageInstalled(pyPackage));
            }
        }
        
        return dependencyStatus;
    }
    
    public static String getInstallationInstructions(Map<String, Boolean> dependencyStatus) {
        StringBuilder instructions = new StringBuilder();
        boolean hasMissing = false;
        
        instructions.append("Installation instructions for missing dependencies:\n\n");
        
        // Check for WSL on Windows
        if (PlatformUtils.isWindows() && !PlatformUtils.isWslInstalled()) {
            instructions.append("Windows Subsystem for Linux (WSL) is not installed. Please install it with these steps:\n");
            instructions.append("1. Open PowerShell as Administrator and run:\n");
            instructions.append("   wsl --install\n");
            instructions.append("2. Restart your computer\n");
            instructions.append("3. Complete the Ubuntu setup when prompted\n\n");
            hasMissing = true;
        }
        
        // Group missing dependencies by installation method
        List<String> aptDependencies = new ArrayList<>();
        List<String> pipDependencies = new ArrayList<>();
        List<String> otherDependencies = new ArrayList<>();
        
        for (Map.Entry<String, Boolean> entry : dependencyStatus.entrySet()) {
            if (!entry.getValue()) {
                String dependency = entry.getKey();
                
                if (REQUIRED_PYTHON_PACKAGES.contains(dependency)) {
                    pipDependencies.add(dependency);
                } else if (Arrays.asList("muscle", "trimal", "iqtree", "python3", "pip").contains(dependency)) {
                    aptDependencies.add(dependency);
                } else {
                    otherDependencies.add(dependency);
                }
                
                hasMissing = true;
            }
        }
        
        // APT installation instructions
        if (!aptDependencies.isEmpty()) {
            instructions.append("For Linux/WSL (Ubuntu/Debian), install with apt:\n");
            instructions.append("sudo apt update && sudo apt install -y ");
            instructions.append(String.join(" ", aptDependencies));
            instructions.append("\n\n");
        }
        
        // PIP installation instructions
        if (!pipDependencies.isEmpty()) {
            instructions.append("For Python packages, install with pip:\n");
            instructions.append("pip install ");
            instructions.append(String.join(" ", pipDependencies));
            instructions.append("\n\n");
        }
        
        // Other dependencies
        if (!otherDependencies.isEmpty()) {
            instructions.append("Other dependencies that need manual installation:\n");
            for (String dep : otherDependencies) {
                if (dep.equals("raxmlHPC-PTHREADS")) {
                    instructions.append("- RAxML: Download from https://github.com/stamatak/standard-RAxML\n");
                } else {
                    instructions.append("- ").append(dep).append("\n");
                }
            }
            instructions.append("\n");
        }
        
        if (!hasMissing) {
            return "All dependencies are installed correctly!";
        }
        
        return instructions.toString();
    }

    /**
     * Get installation instructions for specific dependencies
     * 
     * @param dependencies List of dependency names
     * @return Installation instructions as a string
     */
    public static String getInstallationInstructionsForSpecificDeps(List<String> dependencies) {
        StringBuilder instructions = new StringBuilder();
        
        // Group missing dependencies by installation method
        List<String> aptDependencies = new ArrayList<>();
        List<String> pipDependencies = new ArrayList<>();
        List<String> otherDependencies = new ArrayList<>();
        
        for (String dependency : dependencies) {
            if (REQUIRED_PYTHON_PACKAGES.contains(dependency)) {
                pipDependencies.add(dependency);
            } else if (Arrays.asList("muscle", "trimal", "iqtree", "python3", "pip").contains(dependency)) {
                aptDependencies.add(dependency);
            } else {
                otherDependencies.add(dependency);
            }
        }
        
        // APT installation instructions
        if (!aptDependencies.isEmpty()) {
            instructions.append("For Linux/WSL (Ubuntu/Debian), install with apt:\n");
            instructions.append("sudo apt update && sudo apt install -y ");
            instructions.append(String.join(" ", aptDependencies));
            instructions.append("\n\n");
        }
        
        // PIP installation instructions
        if (!pipDependencies.isEmpty()) {
            instructions.append("For Python packages, install with pip:\n");
            instructions.append("pip install ");
            instructions.append(String.join(" ", pipDependencies));
            instructions.append("\n\n");
        }
        
        // Other dependencies
        if (!otherDependencies.isEmpty()) {
            instructions.append("Other dependencies that need manual installation:\n");
            for (String dep : otherDependencies) {
                if (dep.equals("raxmlHPC-PTHREADS")) {
                    instructions.append("- RAxML: Download from https://github.com/stamatak/standard-RAxML\n");
                } else {
                    instructions.append("- ").append(dep).append("\n");
                }
            }
        }
        
        return instructions.toString();
    }

    private static boolean isDependencyInstalled(String dependency) {
        try {
            Process process;
            if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
                process = new ProcessBuilder("wsl.exe", "which", dependency).start();
            } else if (PlatformUtils.isWindows()) {
                process = new ProcessBuilder("where", dependency).start();
            } else {
                process = new ProcessBuilder("which", dependency).start();
            }
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static boolean isPythonPackageInstalled(String packageName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            if (PlatformUtils.isWindows() && PlatformUtils.isWslInstalled()) {
                processBuilder.command("wsl.exe", "python3", "-c", 
                    "try: import " + packageName + "; print('1') \nexcept ImportError: print('0')");
            } else if (PlatformUtils.isWindows()) {
                processBuilder.command("python", "-c", 
                    "try: import " + packageName + "; print('1') \nexcept ImportError: print('0')");
            } else {
                processBuilder.command("python3", "-c", 
                    "try: import " + packageName + "; print('1') \nexcept ImportError: print('0')");
            }
            
            Process process = processBuilder.start();
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                if (scanner.hasNextLine()) {
                    return scanner.nextLine().equals("1");
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}