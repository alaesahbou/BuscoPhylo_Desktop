import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProcessExecutionServiceTest {

    private ProcessExecutionService processExecutionService;
    private CommandLineUtils commandLineUtils;

    @BeforeEach
    public void setUp() {
        commandLineUtils = Mockito.mock(CommandLineUtils.class);
        processExecutionService = new ProcessExecutionService(commandLineUtils);
    }

    @Test
    public void testExecuteCommandSuccess() {
        String command = "echo Hello World";
        when(commandLineUtils.executeCommand(command)).thenReturn("Hello World");

        String result = processExecutionService.executeCommand(command);

        assertEquals("Hello World", result);
        verify(commandLineUtils, times(1)).executeCommand(command);
    }

    @Test
    public void testExecuteCommandFailure() {
        String command = "invalid_command";
        when(commandLineUtils.executeCommand(command)).thenThrow(new RuntimeException("Command failed"));

        try {
            processExecutionService.executeCommand(command);
        } catch (RuntimeException e) {
            assertEquals("Command failed", e.getMessage());
        }

        verify(commandLineUtils, times(1)).executeCommand(command);
    }
}