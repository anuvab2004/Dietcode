// Test class with dead methods - no compilation errors
public class DeadMethodsSample {

    public static void main(String[] args) {
        liveMethod1();
    }

    public static void liveMethod1() {
        System.out.println("This is live");
    }


    public static void deadMethod1() {
        System.out.println("This is dead code");
        int x = 10;
        int y = 20;
        System.out.println(x + y);
    }


    private void deadMethod2() {
        System.out.println("This is also dead");
        String text = "Hello";
        System.out.println(text.length());
    }

    public void deadMethodChain() {
        deadMethod2();
        System.out.println("Chain called");
    }
}