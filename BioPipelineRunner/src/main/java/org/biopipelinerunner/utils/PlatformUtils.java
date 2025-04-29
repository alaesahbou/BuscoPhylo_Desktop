package org.biopipelinerunner.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class PlatformUtils {

    public static String getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("mac")) {
            return "Mac";
        } else if (os.contains("nix") || os.contains("nux")) {
            return "Linux";
        } else {
            return "Unknown";
        }
    }

    public static boolean isWindows() {
        return getOperatingSystem().equals("Windows");
    }

    public static boolean isMac() {
        return getOperatingSystem().equals("Mac");
    }

    public static boolean isLinux() {
        return getOperatingSystem().equals("Linux");
    }

    public static String getExecutableName(String baseName) {
        if (isWindows()) {
            return baseName + ".exe";
        }
        return baseName;
    }
    
    public static String getOSName() {
        return System.getProperty("os.name");
    }
    
    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }
    
    public static boolean isWslInstalled() {
        if (!isWindows()) {
            return false;
        }
        
        try {
            Process process = Runtime.getRuntime().exec("where wsl.exe");
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    public static String convertWindowsPathToWsl(String windowsPath) {
        if (!isWindows() || !isWslInstalled()) {
            return windowsPath;
        }
        
        // Convert C:\path\to\file to /mnt/c/path/to/file
        if (windowsPath.length() > 2 && windowsPath.charAt(1) == ':') {
            char drive = Character.toLowerCase(windowsPath.charAt(0));
            String pathWithoutDrive = windowsPath.substring(2).replace('\\', '/');
            return "/mnt/" + drive + pathWithoutDrive;
        }
        
        return windowsPath.replace('\\', '/');
    }
    
    public static boolean checkDependency(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (isWindows() && isWslInstalled()) {
                processBuilder.command("wsl.exe", "which", command);
            } else if (isWindows()) {
                processBuilder.command("where", command);
            } else {
                processBuilder.command("which", command);
            }
            
            Process process = processBuilder.start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}