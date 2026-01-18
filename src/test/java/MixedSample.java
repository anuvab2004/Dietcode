// Comprehensive test class - no compilation errors
public class MixedSample {

    private static boolean ALWAYS_FALSE = false;
    private static boolean ALWAYS_TRUE = true;

    public static void main(String[] args) {
        MixedSample sample = new MixedSample();
        sample.publicMethod();
    }

    public void publicMethod() {
        privateMethod();
        staticMethod();
    }

    private void privateMethod() {
        // This is live - called from publicMethod
        System.out.println("Private method");
    }

    public static void staticMethod() {
        // This is live - called from publicMethod
        System.out.println("Static method");
    }

    // Dead method - never called
    public void unusedPublicMethod() {
        System.out.println("Unused");
        helperMethod();
    }

    private void helperMethod() {
        System.out.println("Helper - also dead");
    }

    // Dead method - never called
    private void unusedPrivateMethod() {
        System.out.println("Also unused");
        int calculation = 5 * 10;
        System.out.println(calculation);
    }

    // Method with runtime dead code blocks
    public void methodWithComplexLogic() {
        // Use static field to avoid compile-time detection
        boolean flag = ALWAYS_FALSE;

        // Live block - uses ALWAYS_TRUE
        if (ALWAYS_TRUE) {
            System.out.println("Always executed");
        }

        // Potentially dead block - depends on runtime value
        if (flag) {
            System.out.println("Never executed at runtime");

            // Nested block
            if (ALWAYS_TRUE) {
                System.out.println("Also never executed");
            }
        }

        // Another potentially dead block
        boolean loopCondition = ALWAYS_FALSE;
        while (loopCondition) {
            System.out.println("Loop never runs");
        }

        // Method that's never called within this method
        deadInternalCall();
    }

    private void deadInternalCall() {
        System.out.println("This is dead within the method");
    }

    // Method with switch statement that has dead cases
    public void methodWithSwitch(int value) {
        // value is always 1 in our test
        switch (value) {
            case 1:
                System.out.println("Case 1 - live");
                break;
            case 2:
                System.out.println("Case 2 - dead (never reached)");
                break;
            case 3:
                System.out.println("Case 3 - dead (never reached)");
                break;
            default:
                System.out.println("Default - dead (never reached)");
        }
    }

    // This creates dead code in calling context
    public void testCaller() {
        // Only call with value 1
        methodWithSwitch(1);
        // Dead call - never made
        // methodWithSwitch(2);
    }
}