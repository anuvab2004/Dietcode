import org.junit.Test;
import static org.junit.Assert.*;

public class JUnitTestExample {
    @Test
    public void testAddition() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSubtraction() {
        assertEquals(1, 3 - 2);
    }

    // Not a test - should be marked as dead
    public void helperMethod() {
        System.out.println("Helper");
    }

    // Setup method
    @org.junit.Before
    public void setUp() {
        System.out.println("Setup");
    }

    // Cleanup method
    @org.junit.After
    public void tearDown() {
        System.out.println("Cleanup");
    }
}
