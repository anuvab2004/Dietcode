package test;

public class FieldTest {
    // Used field
    private int used = 10;

    // Unused field
    private String unused = "test";

    public static void main(String[] args) {
        FieldTest test = new FieldTest();
        System.out.println(test.used);
    }

    public void method() {
        // Dead method
        System.out.println("Dead");
    }
}
