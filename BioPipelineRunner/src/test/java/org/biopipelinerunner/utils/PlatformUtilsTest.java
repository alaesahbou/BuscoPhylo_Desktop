import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class PlatformUtilsTest {

    @Test
    public void testIsWindows() {
        try (MockedStatic<PlatformUtils> mockedStatic = Mockito.mockStatic(PlatformUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(PlatformUtils::getOperatingSystem).thenReturn("Windows");
            assertTrue(PlatformUtils.isWindows());
            assertFalse(PlatformUtils.isMac());
            assertFalse(PlatformUtils.isLinux());
        }
    }

    @Test
    public void testIsMac() {
        try (MockedStatic<PlatformUtils> mockedStatic = Mockito.mockStatic(PlatformUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(PlatformUtils::getOperatingSystem).thenReturn("Mac");
            assertTrue(PlatformUtils.isMac());
            assertFalse(PlatformUtils.isWindows());
            assertFalse(PlatformUtils.isLinux());
        }
    }

    @Test
    public void testIsLinux() {
        try (MockedStatic<PlatformUtils> mockedStatic = Mockito.mockStatic(PlatformUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(PlatformUtils::getOperatingSystem).thenReturn("Linux");
            assertTrue(PlatformUtils.isLinux());
            assertFalse(PlatformUtils.isWindows());
            assertFalse(PlatformUtils.isMac());
        }
    }

    @Test
    public void testGetOSName() {
        String osName = PlatformUtils.getOSName();
        assertNotNull(osName);
        assertFalse(osName.isEmpty());
    }

    @Test
    public void testGetOSArchitecture() {
        String osArch = PlatformUtils.getOSArchitecture();
        assertNotNull(osArch);
        assertFalse(osArch.isEmpty());
    }
    
    @Test
    public void testConvertWindowsPathToWsl() {
        try (MockedStatic<PlatformUtils> mockedStatic = Mockito.mockStatic(PlatformUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(PlatformUtils::isWindows).thenReturn(true);
            mockedStatic.when(PlatformUtils::isWslInstalled).thenReturn(true);
            
            String windowsPath = "C:\\Users\\test\\file.txt";
            String wslPath = PlatformUtils.convertWindowsPathToWsl(windowsPath);
            assertEquals("/mnt/c/Users/test/file.txt", wslPath);
            
            // Test path without drive letter
            windowsPath = "\\Users\\test\\file.txt";
            wslPath = PlatformUtils.convertWindowsPathToWsl(windowsPath);
            assertEquals("/Users/test/file.txt", wslPath);
        }
    }
}