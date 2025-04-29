package org.biopipelinerunner.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public static void copyFile(File source, File destination) throws IOException {
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            Files.createDirectories(directory.toPath());
        }
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }
    
    public static void copyResourceToFile(String resourcePath, File destination) throws IOException {
        try (InputStream inputStream = FileUtils.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    public static void copyScriptToWorkingDirectory(String scriptName, File workingDirectory) throws IOException {
        String resourcePath = "/scripts/" + scriptName;
        File destinationFile = new File(workingDirectory, scriptName);
        copyResourceToFile(resourcePath, destinationFile);
        destinationFile.setExecutable(true);
    }
    
    public static void zipDirectory(File directory, File zipFile) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (PlatformUtils.isWindows()) {
            // Use PowerShell to create zip on Windows
            processBuilder.command(
                "powershell.exe", 
                "-Command", 
                "Compress-Archive", 
                "-Path", directory.getAbsolutePath() + "\\*", 
                "-DestinationPath", zipFile.getAbsolutePath()
            );
        } else {
            // Use zip command on Linux/Mac
            processBuilder.command(
                "zip", 
                "-r", 
                zipFile.getAbsolutePath(),
                "."
            );
            processBuilder.directory(directory);
        }
        
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to create zip archive. Exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            throw new IOException("Zip process interrupted", e);
        }
    }
}