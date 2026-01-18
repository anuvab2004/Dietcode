import java.lang.reflect.Method;

public class ReflectionTest {
    public static void main(String[] args) throws Exception {
        // Static call
        normalMethod();

        // Reflection call
        Class clazz = Class.forName("ReflectionTest");
        Method method = clazz.getMethod("reflectionMethod");
        method.invoke(null);
    }

    public static void normalMethod() {
        System.out.println("Normal");
    }

    public static void reflectionMethod() {
        System.out.println("Via reflection");
    }

    public static void deadMethod() {
        System.out.println("Dead");
    }
}
