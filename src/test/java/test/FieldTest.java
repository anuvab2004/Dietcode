package test;

public class FieldTest {
    private int used = 10;
    private String unused = "test";

    public static void main(String[] args) {
        FieldTest test = new FieldTest();
        System.out.println(test.used);
    }

    public void method() {
        System.out.println("Dead");
    }
}
